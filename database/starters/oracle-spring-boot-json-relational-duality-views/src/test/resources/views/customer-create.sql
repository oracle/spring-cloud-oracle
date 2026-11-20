create force editionable json relational duality view customer_dv as customer {
  _id : id
  name
  address {
    _id : id
    city
  }
}