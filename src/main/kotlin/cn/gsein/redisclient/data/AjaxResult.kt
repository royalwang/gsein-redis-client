package cn.gsein.redisclient.data

/**
 * @author G. Seinfeld
 * @since 2021/02/26
 */
class AjaxResult<T>(val code: Int?, val message: String?, val data: T? = null) {
    companion object {
        fun <T> ok(data: T): AjaxResult<T> {
            return AjaxResult(200, "操作成功", data)
        }

        fun <T> error(message: String?): AjaxResult<T> {
            return AjaxResult(500, message)
        }
    }
}