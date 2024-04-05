package edna.chatcenter.demo.appCode.adapters

interface ListItemClickListener {
    fun onClick(position: Int)
    fun onEditItem(position: Int)
    fun onRemoveItem(position: Int)
}
