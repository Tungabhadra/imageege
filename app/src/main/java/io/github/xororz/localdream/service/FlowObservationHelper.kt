package io.github.xororz.localdream.service

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object FlowObservationHelper {

    @JvmStatic
    fun observeGenerationState(
        owner: LifecycleOwner,
        listener: (BackgroundGenerationService.GenerationState) -> Unit
    ) {
        owner.lifecycleScope.launch {
            // Use the static StateFlow from the companion object
            BackgroundGenerationService.generationState.collectLatest { state ->
                listener(state)
            }
        }
    }
}