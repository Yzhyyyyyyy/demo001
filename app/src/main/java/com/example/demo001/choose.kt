package com.example.demo001

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

// 1. 统一的蓝紫渐变背景组件 (固定在底层，绝对不会跟着滑动)
@Composable
fun UnifiedBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // 极简冷白底色
    ) {
        // 魔法光晕效果 (固定背景)
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 右上角浅蓝色
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.65f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.1f),
                    radius = size.width * 0.75f
                )
            )
            // 左下角浅紫色
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD8B4FE).copy(alpha = 0.6f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = size.width * 0.75f
                )
            )
        }
        // 页面具体内容 (浮在背景上面)
        content()
    }
}

// 2. 选择游戏主界面 (增加了 onNavigate 参数用于跳转)
@Composable
fun GameSelectionScreen(onNavigate: (String) -> Unit = {}) {
    UnifiedBackground {
        // 使用 LazyColumn 包裹所有内容，这样整个页面都可以滑动
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp), // 左右边距
            verticalArrangement = Arrangement.spacedBy(16.dp) // 每个元素之间的间距
        ) {
            // 第一项：顶部的标题区域 (也会跟着往上滑)
            item {
                Spacer(modifier = Modifier.height(80.dp)) // 顶部留白

                // 页面大标题 (深藏青色、极粗)
                Text(
                    text = "选择游戏",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2C3040),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 英文副标题
                Text(
                    text = "CHOOSE YOUR GAME",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF94A3B8),
                    letterSpacing = 6.sp,
                )

                Spacer(modifier = Modifier.height(32.dp)) // 标题和列表拉开距离
            }

            // 下面是游戏列表
            // ★ 接入原神页面
            item { GameCard(name = "原神", publisher = "HoYoverse") { onNavigate("genshin") } }

            // ★ 接入星穹铁道页面
            item { GameCard(name = "崩坏：星穹铁道", publisher = "HoYoverse") { onNavigate("star_rail") } }

            item { GameCard(name = "三角洲行动", publisher = "Tencent") { onNavigate("help") } }

            // ★ 接入明日方舟页面
            item { GameCard(name = "明日方舟", publisher = "Hypergryph") { onNavigate("arknights") } }

            // ★ 接入鸣潮页面
            item { GameCard(name = "鸣潮", publisher = "Kuro Games") { onNavigate("wuthering_waves") } }

            item { GameCard(name = "王者荣耀", publisher = "Tencent") { onNavigate("help") } }

            // ★ 重点修改：接入和平精英页面！
            item { GameCard(name = "和平精英", publisher = "Tencent") { onNavigate("peace_elite") } }

            // 重点：实况足球的入口 (正常跳转到 efootball)
            item { GameCard(name = "实况足球", publisher = "KONAMI") { onNavigate("efootball") } }

            // 最后一项：底部留点空白，防止滑到最底下时卡片贴着屏幕边缘
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

// 3. 游戏卡片组件 (增加了 onClick 点击事件)
@Composable
fun GameCard(name: String, publisher: String, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable { onClick() }, // 绑定点击事件
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左边的文字部分
            Column {
                Text(
                    text = name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF2C3040)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = publisher,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            // 右边的小箭头图标 (亮蓝色点缀)
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "进入",
                tint = Color(0xFF4661F6)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameSelectionScreenPreview() {
    GameSelectionScreen()
}