package com.example.demo001

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyFirstScreen(onStartClick: () -> Unit = {}) {
    // 整个屏幕的底层容器
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // 极简冷白/灰白背景
    ) {
        // 1. 魔法光晕效果 (增强版)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 右上角的浅蓝色柔光
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.1f),
                    radius = size.width * 0.75f
                )
            )
            // 左下角的浅紫色柔光
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD8B4FE).copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = size.width * 0.75f
                )
            )
        }

        // 2. 核心内容区
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 顶部 Logo 容器：纯白圆底 + 极浅灰边框
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(Color.White, CircleShape)
                    .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Logo",
                    tint = Color(0xFF0F172A), // 深色图标
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 主标题：极深灰（近黑），升级为终极加粗的 Black 级别！
            Text(
                text = "抽卡模拟器",
                fontSize = 44.sp,
                fontWeight = FontWeight.Black, // <--- 换成了最粗的 Black！
                color = Color(0xFF0F172A),
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 副标题：中灰色
            Text(
                text = "GACHA SIMULATOR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                letterSpacing = 6.sp
            )

            // 按钮上方的间距
            Spacer(modifier = Modifier.height(80.dp))

            // 极简高对比度按钮
            Button(
                onClick = {
                    onStartClick() // <--- 【修改点】这里调用跳转指令
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F172A), // 极深灰色底色
                    contentColor = Color.White          // 纯白文字
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = "选择游戏",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 8.sp // 宽字间距
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyFirstScreenPreview() {
    MyFirstScreen()
}
