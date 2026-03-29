package com.spellwriter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.request.ImageRequest
import com.spellwriter.R

/**
 * GIF reward overlay displayed after star achievement completion.
 *
 * Shows a fullscreen overlay with:
 * - Semi-transparent black background (0.5 alpha)
 * - Animated GIF from assets (80% screen size, centered)
 * - "Continue" button at bottom center (user-controlled progression)
 *
 * @param gifAssetPath Path to GIF file in assets folder (e.g., "gifs/cat1.gif")
 * @param onContinue Callback when user taps Continue button
 * @param modifier Modifier for the overlay container
 */
@Composable
fun GifRewardOverlay(
    gifAssetPath: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Fullscreen overlay with semi-transparent background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        // GIF display using Coil
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/$gifAssetPath")
                .decoderFactory(GifDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.celebration_gif_description),
            modifier = Modifier.fillMaxSize(0.8f), // 80% of screen size
            contentScale = ContentScale.Fit
        )

        // Continue button at bottom center
        Button(
            onClick = onContinue,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = stringResource(R.string.celebration_continue),
                fontSize = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        }
    }
}
