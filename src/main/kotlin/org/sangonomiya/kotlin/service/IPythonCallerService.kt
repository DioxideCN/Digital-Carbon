package org.sangonomiya.kotlin.service

import org.sangonomiya.app.core.ResponseBounce
import org.springframework.stereotype.Service

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/10 0:24
 * @since 1.0
 */
@Service
interface IPythonCallerService {
    fun requestProduct(username: String, companyId: Int, productId: Int): ResponseBounce<Any>
    fun requestTotal(username: String, companyId: Int): ResponseBounce<Any>
}