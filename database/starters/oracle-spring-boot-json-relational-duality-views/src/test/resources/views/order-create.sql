create force editionable json relational duality view orders_dv as orders @insert {
  _id : order_id
  product : products {
    _id : product_id
    name
    price
  }
  quantity
  orderDate : order_date
}