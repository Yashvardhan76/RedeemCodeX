package me.justlime.redeemcodex.enums

sealed interface JPermission {
    data object Admin : JPermission {
        private const val USE = "redeemx.admin.use"
        const val GEN = "$USE.gen"
        const val MODIFY = "$USE.modify"
        const val DELETE = "$USE.delete"
        const val PREVIEW = "$USE.preview"
        const val INFO = "$USE.info"
        const val RENEW = "$USE.renew"
        const val RELOAD = "$USE.reload"
        const val USAGE = "$USE.usage"
    }

    data object Player : JPermission {
        const val USE = "redeemx.use"
    }

}