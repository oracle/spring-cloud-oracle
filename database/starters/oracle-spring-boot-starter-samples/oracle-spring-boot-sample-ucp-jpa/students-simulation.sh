#!/usr/bin/env bash
# Copyright (c) 2026, Oracle and/or its affiliates.
# Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/.

set -u

student_api_url="${STUDENT_API_URL:-http://localhost:9001}"
student_api_url="${student_api_url%/}"
request_interval_seconds="${REQUEST_INTERVAL_SECONDS:-1}"
burst_size="${BURST_SIZE:-50}"
max_students="${MAX_STUDENTS:-25}"

first_names=(Demo Load Sample Simulated Synthetic Test)
last_names=(Client Record Request Student User Worker)
majors=("Computer Science" Mathematics Engineering "Data Science")
gpas=(2.5 2.8 3.0 3.2 3.5 3.8 4.0)
student_ids=()
cycle=0

curl_options=(
    --connect-timeout 3
    --fail
    --max-time 10
    --show-error
    --silent
)

validate_positive_integer() {
    local name="$1"
    local value="$2"

    if [[ ! "$value" =~ ^[1-9][0-9]*$ ]]; then
        printf '%s must be a positive integer, but was %s\n' "$name" "$value" >&2
        exit 1
    fi
}

validate_interval() {
    if [[ ! "$request_interval_seconds" =~ ^[0-9]+([.][0-9]+)?$ ]]; then
        printf 'REQUEST_INTERVAL_SECONDS must be a non-negative number, but was %s\n' \
            "$request_interval_seconds" >&2
        exit 1
    fi
}

wait_for_application() {
    printf 'Waiting for the student API at %s' "$student_api_url"
    until curl --connect-timeout 2 --fail --max-time 3 --silent --output /dev/null \
        "$student_api_url/student"; do
        printf '.'
        sleep 2
    done
    printf ' ready\n'
}

extract_student_id() {
    sed -n 's/.*"id"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p'
}

generate_student_payload() {
    local request_number="$1"
    local first_name="${first_names[RANDOM % ${#first_names[@]}]}"
    local last_name="${last_names[RANDOM % ${#last_names[@]}]}"
    local major="${majors[RANDOM % ${#majors[@]}]}"
    local gpa="${gpas[RANDOM % ${#gpas[@]}]}"
    local credits=$((RANDOM % 121))
    local payload

    printf -v payload \
        '{"firstName":"%s","lastName":"%s","email":"student-%d-%d@example.com","major":"%s","credits":%d,"gpa":%s}' \
        "$first_name" "$last_name" "$cycle" "$request_number" "$major" "$credits" "$gpa"
    printf '%s' "$payload"
}

remove_tracked_student() {
    local student_id_to_remove="$1"
    local student_index

    for ((student_index = 0; student_index < ${#student_ids[@]}; student_index++)); do
        if [[ "${student_ids[student_index]}" == "$student_id_to_remove" ]]; then
            student_ids=("${student_ids[@]:0:student_index}" \
                "${student_ids[@]:student_index + 1}")
            break
        fi
    done
}

run_mixed_burst() {
    local request_number
    local operation_index
    local operation_offset
    local operation
    local target_student_id=""
    local post_response_file=""
    local response_dir
    local request_index
    local delete_index
    local target_index
    local created_student_id
    local student_id
    local reserved
    local -a available_delete_ids=("${student_ids[@]}")
    local -a read_student_ids=()
    local -a request_pids=()
    local -a request_operations=()
    local -a request_targets=()
    local -a post_response_files=()

    last_post_count=0
    last_list_get_count=0
    last_single_get_count=0
    last_delete_count=0

    if ! response_dir=$(mktemp -d "${TMPDIR:-/tmp}/students-simulation.XXXXXX"); then
        printf 'Cycle %d: unable to create a temporary response directory.\n' "$cycle" >&2
        return 1
    fi

    # Keep one tracked record available for successful GET-by-ID requests.
    if ((${#available_delete_ids[@]} > 1)); then
        delete_index=$((RANDOM % ${#available_delete_ids[@]}))
        available_delete_ids=("${available_delete_ids[@]:0:delete_index}" \
            "${available_delete_ids[@]:delete_index + 1}")
    fi

    operation_offset=$((RANDOM % 4))

    # Plan the whole batch first. Delete targets must be reserved before choosing
    # GET-by-ID targets, otherwise concurrent reads can intentionally race into 404s.
    for ((request_number = 1; request_number <= burst_size; request_number++)); do
        operation_index=$(( (request_number + operation_offset) % 4 ))
        case "$operation_index" in
            0)
                operation='POST'
                ;;
            1)
                operation='GET_LIST'
                ;;
            2)
                operation='GET_ONE'
                ;;
            *)
                operation='DELETE'
                ;;
        esac

        request_operations+=("$operation")
        request_targets+=("")
        post_response_files+=("")
    done

    for ((request_index = 0; request_index < ${#request_operations[@]}; request_index++)); do
        if [[ "${request_operations[request_index]}" == 'DELETE' ]]; then
            if ((${#available_delete_ids[@]} > 0)); then
                delete_index=$((RANDOM % ${#available_delete_ids[@]}))
                request_targets[request_index]="${available_delete_ids[delete_index]}"
                available_delete_ids=("${available_delete_ids[@]:0:delete_index}" \
                    "${available_delete_ids[@]:delete_index + 1}")
            else
                request_operations[request_index]='GET_LIST'
            fi
        fi
    done

    # Exclude reserved delete targets from single-record reads in this batch.
    for student_id in "${student_ids[@]}"; do
        reserved=0
        for ((request_index = 0; request_index < ${#request_operations[@]}; request_index++)); do
            if [[ "${request_operations[request_index]}" == 'DELETE' && \
                "${request_targets[request_index]}" == "$student_id" ]]; then
                reserved=1
                break
            fi
        done
        if ((reserved == 0)); then
            read_student_ids+=("$student_id")
        fi
    done

    for ((request_index = 0; request_index < ${#request_operations[@]}; request_index++)); do
        if [[ "${request_operations[request_index]}" == 'GET_ONE' ]]; then
            if ((${#read_student_ids[@]} > 0)); then
                target_index=$((RANDOM % ${#read_student_ids[@]}))
                request_targets[request_index]="${read_student_ids[target_index]}"
            else
                request_operations[request_index]='GET_LIST'
            fi
        fi
    done

    for ((request_index = 0; request_index < ${#request_operations[@]}; request_index++)); do
        operation="${request_operations[request_index]}"
        target_student_id="${request_targets[request_index]}"

        case "$operation" in
            POST)
                post_response_file="$response_dir/post-$((request_index + 1)).json"
                post_response_files[request_index]="$post_response_file"
                payload=$(generate_student_payload "$((request_index + 1))")
                curl "${curl_options[@]}" --header 'Content-Type: application/json' \
                    --request POST \
                    --data "$payload" \
                    --output "$post_response_file" \
                    "$student_api_url/student" &
                last_post_count=$((last_post_count + 1))
                ;;
            GET_LIST)
                curl "${curl_options[@]}" --output /dev/null \
                    "$student_api_url/student" &
                last_list_get_count=$((last_list_get_count + 1))
                ;;
            GET_ONE)
                curl "${curl_options[@]}" --output /dev/null \
                    "$student_api_url/student/$target_student_id" &
                last_single_get_count=$((last_single_get_count + 1))
                ;;
            DELETE)
                curl "${curl_options[@]}" --output /dev/null \
                    --request DELETE "$student_api_url/student/$target_student_id" &
                last_delete_count=$((last_delete_count + 1))
                ;;
        esac

        request_pids+=("$!")
    done

    for ((request_index = 0; request_index < ${#request_pids[@]}; request_index++)); do
        if wait "${request_pids[request_index]}"; then
            case "${request_operations[request_index]}" in
                POST)
                    if [[ -r "${post_response_files[request_index]}" ]]; then
                        created_student_id=$(extract_student_id < "${post_response_files[request_index]}")
                        if [[ -n "$created_student_id" ]]; then
                            student_ids+=("$created_student_id")
                        fi
                    fi
                    ;;
                DELETE)
                    remove_tracked_student "${request_targets[request_index]}"
                    ;;
            esac
        fi

        if [[ -n "${post_response_files[request_index]}" ]]; then
            rm -f "${post_response_files[request_index]}"
        fi
    done

    rmdir "$response_dir" 2>/dev/null || true
}

stop_simulation() {
    printf '\nStudent simulation stopped after %d cycles.\n' "$cycle"
    exit 0
}

if ! command -v curl >/dev/null 2>&1; then
    printf 'curl is required to run the student simulation.\n' >&2
    exit 1
fi

validate_positive_integer BURST_SIZE "$burst_size"
validate_positive_integer MAX_STUDENTS "$max_students"
validate_interval
trap stop_simulation INT TERM

wait_for_application
printf 'Generating concurrent mixed student activity with burst size %d; batches run back-to-back.\n' \
    "$burst_size"

while true; do
    cycle=$((cycle + 1))

    if ((${#student_ids[@]} == 0)); then
        payload=$(generate_student_payload 0)
        if ! create_response=$(curl "${curl_options[@]}" \
            --header 'Content-Type: application/json' \
            --request POST \
            --data "$payload" \
            "$student_api_url/student"); then
            printf 'Cycle %d: failed to create the initial student; retrying next cycle.\n' "$cycle" >&2
            sleep "$request_interval_seconds"
            continue
        fi

        student_id=$(printf '%s' "$create_response" | extract_student_id)
        if [[ -z "$student_id" ]]; then
            printf 'Cycle %d: initial create response did not contain a student ID.\n' "$cycle" >&2
            sleep "$request_interval_seconds"
            continue
        fi

        student_ids+=("$student_id")
    fi

    if ! run_mixed_burst; then
        printf 'Cycle %d: failed to prepare the concurrent request batch; retrying next cycle.\n' "$cycle" >&2
        sleep "$request_interval_seconds"
        continue
    fi

    while ((${#student_ids[@]} > max_students)); do
        oldest_student_id="${student_ids[0]}"
        if curl "${curl_options[@]}" --output /dev/null \
            --request DELETE "$student_api_url/student/$oldest_student_id"; then
            student_ids=("${student_ids[@]:1}")
        else
            break
        fi
    done

    printf 'Cycle %d: sent %d concurrent requests (POST=%d, list GET=%d, single GET=%d, DELETE=%d); tracking %d student(s).\n' \
        "$cycle" "$burst_size" "$last_post_count" "$last_list_get_count" \
        "$last_single_get_count" "$last_delete_count" "${#student_ids[@]}"
done
