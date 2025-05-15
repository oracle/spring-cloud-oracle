create force editionable json relational duality view student_dv as student @insert @update @delete {
  _id : id
  firstName : first_name
  lastName : last_name
  email
  major
  credits
  gpa
}