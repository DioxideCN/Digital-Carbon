package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.ICollectorService
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 * @author Dioxide.CN
 * @date 2023/4/9 8:54
 * @since 1.0
 */
@Api(tags = ["PortalController"])
@RestController
class PortalController {

    @Resource
    private lateinit var collectorService: ICollectorService

    @ApiOperation(value = "向应用网关推送消息")
    @PostMapping("/application/portal")
    fun getCodeAction(
        @RequestParam("appId") appId: String,
        @RequestParam("data") data: String
    ): ResponseBounce<Any> {
        return collectorService.applicationPortal(
            appId,
            data
        )
    }

}