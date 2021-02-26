package cn.gsein.redisclient.service

import cn.gsein.redisclient.data.ConnectionData
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.StatefulRedisConnection
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.Resource

/**
 * @author G. Seinfeld
 * @since 2021/02/19
 */
@Service
class RedisService {
    companion object {
        var connectionMap: MutableMap<String, MutableMap<Int, StatefulRedisConnection<String, String>>> =
            ConcurrentHashMap()

        var redisAddressMap: MutableMap<String, ConnectionData> = ConcurrentHashMap()
    }

    @Resource
    lateinit var localStorageService: LocalStorageService

    fun listAddresses(): MutableMap<String, ConnectionData> {
        return redisAddressMap
    }

    fun registerNewRedisAddress(connectionData: ConnectionData) {
        var exists = false
        var key = ""

        // 覆盖原有的连接信息
        for (entry in redisAddressMap) {
            if (connectionData == entry.value) {
                redisAddressMap[entry.key] = connectionData
                exists = true
                key = entry.key
                break
            }
        }

        // 添加新的连接信息
        if (!exists) {
            val uuid = nextUuid()
            redisAddressMap[uuid] = connectionData
            key = uuid
        }

        // 创建连接实体的缓存映射
        if (!connectionMap.containsKey(key)) {
            connectionMap[key] = ConcurrentHashMap()
        }

        // 将连接信息同步到本地文件，以便下次使用
        localStorageService.synchronize(redisAddressMap)
    }

    fun getConnection(uuid: String, database: Int): StatefulRedisConnection<String, String> {
        val connection = connectionMap[uuid]?.get(database)
        return if (connection != null) {
            connection
        } else {
            val connectionData = redisAddressMap[uuid]
            val builder = RedisURI.builder()
            val uri = with(builder) {
                withHost(connectionData?.host)
                connectionData?.port?.let { withPort(it) }
                withPassword(connectionData?.password?.toCharArray())
                withDatabase(database)
                build()
            }

            val client = RedisClient.create(uri)
            client.connect()
        }
    }

    private fun nextUuid(): String {
        var uuid: String
        do {
            uuid = UUID.randomUUID().toString()
        } while (redisAddressMap.containsKey(uuid))
        return uuid
    }

    fun info(): Map<String, String> {
//        val info = connection.sync().info()
//        return info.split("\n").filter {
//            it.contains(":")
//        }.map {
//            it.split(":")
//        }.associateBy({ it[0] }, { it[1].trim() })
        return HashMap()
    }
}