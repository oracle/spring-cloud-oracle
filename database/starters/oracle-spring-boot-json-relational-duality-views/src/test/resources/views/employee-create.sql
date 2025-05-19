create force editionable json relational duality view employee_dv as employee @insert @update @delete {
  _id : id
  name
  manager : employee @insert @update @link (from : [manager_id]) {
    _id : id
    name
  }
  reports : employee @insert @update {
    _id : id
    name
  }
}