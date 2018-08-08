package com.jurcikova.ivet.countries.mvi.common

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

@GlideModule
class SvgModule : AppGlideModule() {

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.register(SVG::class.java, PictureDrawable::class.java, SvgDrawableTranscoder()).append(InputStream::class.java, SVG::class.java, SvgDecoder())
    }

    // Disable manifest parsing to avoid adding similar modules twice.
    override fun isManifestParsingEnabled(): Boolean = false
}

class SvgSoftwareLayerSetter : RequestListener<PictureDrawable> {

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<PictureDrawable>?, isFirstResource: Boolean): Boolean {
        (target as ImageViewTarget).view.run {
            setLayerType(ImageView.LAYER_TYPE_NONE, null)
        }

        return false
    }

    override fun onResourceReady(resource: PictureDrawable?, model: Any?, target: Target<PictureDrawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        (target as ImageViewTarget).view.run {
            setLayerType(ImageView.LAYER_TYPE_NONE, null)
        }

        return false
    }
}

class SvgDrawableTranscoder : ResourceTranscoder<SVG, PictureDrawable> {

    override fun transcode(toTranscode: Resource<SVG>, options: Options): Resource<PictureDrawable>? =
            SimpleResource(PictureDrawable(toTranscode.get().renderToPicture()))
}

class SvgDecoder : ResourceDecoder<InputStream, SVG> {

    override fun handles(source: InputStream, options: Options): Boolean = true

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG>? =
            try {
                SimpleResource(SVG.getFromInputStream(source))
            } catch (ex: SVGParseException) {
                throw IOException("Cannot load SVG from stream, ex")
            }
}

