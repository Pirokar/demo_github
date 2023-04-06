package io.edna.threads.demo.adapters.serverList

import io.edna.threads.demo.models.ServerConfig

interface ServerListItemOnClickListener {
    fun onClick(item: ServerConfig)
    fun onEditItem(item: ServerConfig)
    fun onRemoveItem(item: ServerConfig)
}
