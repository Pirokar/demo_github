package im.threads.business.rest.models

data class VersionsModel(
    val build: VersionItemModel?,
    val datastore: VersionItemModel?,
    val gate: VersionItemModel?
)
