package org.sangonomiya.kotlin.controller

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.sangonomiya.app.core.ResponseBounce
import org.sangonomiya.app.service.INotificationService
import org.springframework.web.bind.annotation.*
import javax.annotation.Resource

/**
 *
 * @author Dioxide.CN
 * @date 2023/4/8 16:48
 * @since 1.0
 */
@Api(tags = ["NotificationController"])
@RestController
@RequestMapping("/api/notification")
class NotificationController {
    
    @Resource
    private lateinit var notificationService: INotificationService
    
    @ApiOperation(value = "获取指定页的消息列表")
    @GetMapping("/message/view")
    fun getAllMessagePage(
        @RequestParam("username") username: String,
        @RequestParam("page") page: Int
    ): ResponseBounce<Any> {
        return notificationService.getUserRecentNotification(username, page)
    }

    @ApiOperation(value = "为顶部组件获取最近消息")
    @GetMapping("/message/recent")
    fun getComponentMessagePage(
        @RequestParam("username") username: String
    ): ResponseBounce<Any> {
        return notificationService.getUserSimpleNotification(username)
    }

    @ApiOperation(value = "获取用户消息的详细信息")
    @GetMapping("/message/query")
    fun getNotificationById(
        @RequestParam("username") username: String,
        @RequestParam("messageId") messageId: Int
    ): ResponseBounce<Any> {
        return notificationService.getNotificationDetail(username, messageId)
    }

    @ApiOperation(value = "标记已读")
    @PostMapping("/message/mark-as-read")
    fun markAsRead(
        @RequestParam("username") username: String,
        @RequestParam("messageId") messageId: Int
    ): ResponseBounce<Any> {
        return notificationService.markMessageAsRead(username, messageId)
    }

    @ApiOperation(value = "删除消息")
    @PostMapping("/message/delete")
    fun deleteMessage(
        @RequestParam("username") username: String,
        @RequestParam("messageId") messageId: Int
    ): ResponseBounce<Any> {
        return notificationService.deleteMessage(username, messageId)
    }

    @ApiOperation(value = "批量标记已读")
    @PostMapping("/message/batch/mark-as-read")
    fun batchMarkAsRead(
        @RequestParam("username") username: String,
        @RequestBody awaitMessage: IntArray
    ): ResponseBounce<Any> {
        return notificationService.batchMarkMessageAsRead(username, awaitMessage)
    }

    @ApiOperation(value = "批量删除消息")
    @PostMapping("/message/batch/delete")
    fun batchDeleteMessage(
        @RequestParam("username") username: String,
        @RequestBody awaitMessage: IntArray
    ): ResponseBounce<Any> {
        return notificationService.batchDeleteMessage(username, awaitMessage)
    }
}
