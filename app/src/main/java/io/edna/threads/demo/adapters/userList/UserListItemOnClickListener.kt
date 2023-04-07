package io.edna.threads.demo.adapters.userList

import io.edna.threads.demo.models.UserInfo

interface UserListItemOnClickListener {
    fun onClick(item: UserInfo)
    fun onEditItem(item: UserInfo)
    fun onRemoveItem(item: UserInfo)
}
