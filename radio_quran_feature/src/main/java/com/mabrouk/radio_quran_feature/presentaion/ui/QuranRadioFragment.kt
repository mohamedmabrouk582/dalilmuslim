package com.mabrouk.radio_quran_feature.presentaion.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.mabrouk.radio_quran_feature.R
import com.mabrouk.radio_quran_feature.databinding.QuranRadioLayoutBinding
import com.mabrouk.radio_quran_feature.presentaion.viewmodels.RadioStates
import com.mabrouk.radio_quran_feature.presentaion.viewmodels.RadioViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * @name Mohamed Mabrouk
 * Copyright (c) 4/20/22
 */
@AndroidEntryPoint
class QuranRadioFragment : Fragment() {
    lateinit var viewBinding: QuranRadioLayoutBinding
    val viewModel: RadioViewModel by viewModels()
    private val player by lazy {
        ExoPlayer.Builder(requireContext()).setMediaSourceFactory(
            DefaultMediaSourceFactory(requireContext()).setLiveTargetOffsetMs(5000)
        ).build()
    }
    private val adapter by lazy {
        RadioAdapter {
            player.clearMediaItems()
            player.addMediaItem(addLiveMediaItem(it.radio_url))
            player.seekTo(playbackPosition)
            player.prepare()
            player.play()
            viewBinding.name = it.name
        }
    }
    var playbackPosition: Long = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = DataBindingUtil.inflate(inflater,R.layout.quran_radio_layout,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        player.seekTo(playbackPosition)
        viewModel.requestRadios()
        viewBinding.rcv.adapter = adapter
        val layoutManager = FlexboxLayoutManager(requireContext())
        layoutManager.flexDirection = FlexDirection.ROW
        viewBinding.rcv.layoutManager = layoutManager

        player.addMediaItem(addLiveMediaItem("http://live.mp3quran.net:9722/;"))
        player.prepare()
        player.play()
        handleStates()
    }

    private fun handleStates() {
        lifecycleScope.launch {
            viewModel.states.collect {
                when (it) {
                    RadioStates.Idle -> {}
                    is RadioStates.LoadData -> {
                        adapter.items = it.data
                        viewBinding.progress.visibility = View.GONE
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackPosition = player.currentPosition
        player.release()
    }


    private fun addLiveMediaItem(uri: String): MediaItem {
        return MediaItem.Builder()
            .setUri(uri)
            .setLiveConfiguration(
                MediaItem.LiveConfiguration.Builder()
                    .setMaxPlaybackSpeed(1.02f)
                    .build()
            )
            .build()
    }

}