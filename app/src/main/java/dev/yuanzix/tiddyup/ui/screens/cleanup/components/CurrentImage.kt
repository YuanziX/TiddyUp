import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CurrentImage(
    imageUri: Uri,
    onRightSwipe: () -> Unit, // Go to next image
    onLeftSwipe: () -> Unit,  // Add to delete list and go to next image
) {
    val config = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidth / 4f
    val animatableOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val swipeDirection =
        remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height((config.screenHeightDp * 0.6f).dp)
            .draggable(
                orientation = Orientation.Horizontal,
                state = rememberDraggableState { delta ->
                    coroutineScope.launch {
                        animatableOffset.snapTo(animatableOffset.value + delta)
                    }
                    swipeDirection.floatValue = if (delta > 0) 1f else -1f
                },
                onDragStopped = {
                    coroutineScope.launch {
                        if (animatableOffset.value > swipeThreshold) {
                            onRightSwipe()
                            animatableOffset.snapTo(0f)
                        } else if (animatableOffset.value < -swipeThreshold) {
                            onLeftSwipe()
                            animatableOffset.snapTo(0f)
                        } else {
                            animatableOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessLow)
                            )
                        }
                    }
                }
            )
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .offset { IntOffset(animatableOffset.value.roundToInt(), 0) }
                .fillMaxSize()
        )

        val leftSwipeAlpha = (animatableOffset.value / -swipeThreshold).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = leftSwipeAlpha),
                    shape = MaterialTheme.shapes.small
                )
                .size(75.dp)
                .alpha(leftSwipeAlpha)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        val rightSwipeAlpha = (animatableOffset.value / swipeThreshold).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = rightSwipeAlpha),
                    shape = MaterialTheme.shapes.small
                )
                .size(75.dp)
                .alpha(rightSwipeAlpha)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}
