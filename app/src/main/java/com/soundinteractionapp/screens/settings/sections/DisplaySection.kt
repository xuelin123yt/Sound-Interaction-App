package com.soundinteractionapp.screens.settings.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Resolution(val width: Int, val height: Int) {
    override fun toString(): String = "${width}×${height}"
}

@Composable
fun DisplaySection() {
    val configuration = LocalConfiguration.current

    // 計算裝置實際解析度（像素）
    val deviceWidth = remember { configuration.screenWidthDp * configuration.densityDpi / 160 }
    val deviceHeight = remember { configuration.screenHeightDp * configuration.densityDpi / 160 }
    val deviceResolution = remember { Resolution(deviceWidth, deviceHeight) }

    // 渲染解析度（這裡假設與裝置相同，您可以根據需求調整）
    val renderResolution = remember { Resolution(deviceWidth, deviceHeight) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ResolutionCard(
            deviceResolution = deviceResolution,
            renderResolution = renderResolution
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ResolutionCard(
    deviceResolution: Resolution,
    renderResolution: Resolution
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "渲染解析度",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "裝置", value = deviceResolution.toString())
                InfoItem(label = "渲染", value = renderResolution.toString())
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF666666)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2196F3)
        )
    }
}