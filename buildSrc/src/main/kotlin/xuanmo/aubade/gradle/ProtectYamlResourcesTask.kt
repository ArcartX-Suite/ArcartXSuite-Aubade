package xuanmo.aubade.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * YAML 资源保护任务。
 * 将 src/main/resources 中的非 plugin.yml YAML 文件加密为 .axb 格式，
 * 输出到 build/resources/main/arcartx/internal/protected/。
 *
 * 注：此为简化版骨架，实际加密逻辑待 AXS 规范确定后完善。
 */
abstract class ProtectYamlResourcesTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun protect() {
        val src = sourceDir.get().asFile
        val out = outputDir.get().asFile
        if (!src.exists()) {
            logger.info("[ProtectYaml] 源目录不存在，跳过: ${src.absolutePath}")
            return
        }

        out.mkdirs()

        src.walkTopDown()
            .filter { it.isFile && it.extension.equals("yml", ignoreCase = true) }
            .filter { it.name != "plugin.yml" }
            .forEach { file ->
                val relativePath = file.relativeTo(src).path
                val targetFile = File(out, relativePath.replace(".yml", ".axb").replace(".yaml", ".axb"))
                targetFile.parentFile.mkdirs()

                // 简化版：仅做异或混淆（实际应使用 AES）
                val data = file.readBytes()
                val obfuscated = simpleXor(data, "AubadeSalt".toByteArray())
                targetFile.writeBytes(obfuscated)

                logger.info("[ProtectYaml] 已保护: $relativePath -> ${targetFile.name}")
            }

        logger.lifecycle("[ProtectYaml] YAML 资源保护完成，输出到: ${out.absolutePath}")
    }

    private fun simpleXor(data: ByteArray, key: ByteArray): ByteArray {
        return ByteArray(data.size) { i ->
            (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
    }
}
