package code.name.monkey.companion.mvp.model

import org.jaudiotagger.tag.FieldKey

data class LoadingInfo(val filePaths: Collection<String>,
                       val fieldKeyValueMap: Map<FieldKey, String>,
                       val artworkInfo: ArtworkInfo?)