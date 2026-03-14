package com.ifochka.m14n.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import m14n.frontend.composeapp.generated.resources.Res
import m14n.frontend.composeapp.generated.resources.ic_apple_logo_white
import m14n.frontend.composeapp.generated.resources.ic_google
import org.jetbrains.compose.resources.painterResource

private val buttonShape = RoundedCornerShape(4.dp)
private val buttonHeight = Modifier.fillMaxWidth().height(44.dp)
private val buttonPadding = PaddingValues(horizontal = 16.dp)
private const val BUTTON_FONT_SIZE = 14

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonHeight,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F1F1F),
            disabledContainerColor = Color(0xFFF2F2F2),
            disabledContentColor = Color(0xFF1F1F1F),
        ),
        border = BorderStroke(width = 1.dp, color = Color(0xFF747775)),
        contentPadding = buttonPadding,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Continue with Google", fontSize = BUTTON_FONT_SIZE.sp, maxLines = 1)
        }
    }
}

@Composable
fun AppleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = buttonHeight,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF333333),
            disabledContentColor = Color.White,
        ),
        contentPadding = buttonPadding,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(Res.drawable.ic_apple_logo_white),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(12.dp))
            Text(text = "Continue with Apple", fontSize = BUTTON_FONT_SIZE.sp, maxLines = 1)
        }
    }
}
