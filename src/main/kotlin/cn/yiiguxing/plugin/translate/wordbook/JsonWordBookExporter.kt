package cn.yiiguxing.plugin.translate.wordbook

import cn.yiiguxing.plugin.translate.util.registerDateTypeAdapter
import com.google.gson.GsonBuilder
import java.io.OutputStream
import java.io.OutputStreamWriter

class JsonWordBookExporter : WordBookExporter {

    override val name: String = "JSON"

    override val extension: String = "json"

    override val availableForImport: Boolean = true

    override fun export(words: List<WordBookItem>, outputStream: OutputStream) {
        OutputStreamWriter(outputStream, Charsets.UTF_8).use { writer ->
            GsonBuilder()
                .registerDateTypeAdapter()
                .setPrettyPrinting()
                .create()
                .toJson(words, writer)
            writer.flush()
        }
    }
}