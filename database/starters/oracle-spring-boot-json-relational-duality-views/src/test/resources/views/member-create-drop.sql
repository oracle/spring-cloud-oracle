create force editionable json relational duality view members_dv as members @insert @update @delete {
  _id : member_id
  fullName : name
  loans @insert @update {
    _id : loan_id
    book : books @insert @update @link (from : [book_id]) {
      _id : book_id
      title
    }
  }
}