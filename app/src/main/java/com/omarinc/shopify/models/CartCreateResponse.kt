package com.omarinc.shopify.models

data class CartCreateResponse(
    val data: Data
) {
    data class Data(
        val cartCreate: CartCreate
    ) {
        data class CartCreate(
            val cart: Cart
        ) {
            data class Cart(
                val id: String,
                val lines: Lines
            ) {
                data class Lines(
                    val edges: List<Edge>
                ) {
                    data class Edge(
                        val node: Node?
                    ) {
                        data class Node(
                            val id: String,
                            val quantity: Int,
                            val merchandise: Merchandise
                        ) {
                            data class Merchandise(
                                val __typename: String
                            )
                        }
                    }
                }
            }
        }
    }
}
