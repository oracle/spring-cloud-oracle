create force editionable json relational duality view orders_dv as orders @insert {
  _id : order_id
  quantity
  orderDate : order_date
  product : products {
    _id : product_id
    name
    price
  }
}