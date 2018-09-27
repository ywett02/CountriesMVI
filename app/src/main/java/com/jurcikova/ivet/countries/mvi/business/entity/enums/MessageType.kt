package com.jurcikova.ivet.countries.mvi.business.entity.enums

sealed class MessageType() {
    object AddToFavorite: MessageType()
    object RemoveFromFavorite: MessageType()
}