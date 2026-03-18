package com.example.demo001

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HelpScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // 保持与主界面一致的底层背景色
    ) {
        // 1. 保持一致的背景流光特效
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.1f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFD8B4FE).copy(alpha = 0.4f), Color.Transparent),
                    center = Offset(size.width * 0.2f, size.height * 0.8f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 2. 顶部返回按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF0F172A))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "模块开发中",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0F172A)
                )
            }

            // 3. 主体内容卡片
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 顶部图标
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(Color(0xFFF1F5F9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Build,
                                contentDescription = null,
                                tint = Color(0xFF4661F6),
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "需要你的帮助！",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0F172A)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "该游戏的模拟抽卡模块尚未实装。为了完美还原最真实的抽卡体验，我们需要收集以下核心数据：",
                            fontSize = 15.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 需求列表
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RequirementItem("🎲", "详细的抽卡机制与概率分布")
                            RequirementItem("💰", "真实的充值档位与赠送比例")
                            RequirementItem("✨", "特殊的游戏特性（如保底等）")
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Divider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "如果你是这款游戏的资深玩家，\n欢迎联系作者提供数据支持！",
                            fontSize = 14.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 联系作者按钮 (升级为直接拉起邮件App)
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:3242752034@qq.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "关于模拟抽卡模块的数据提供")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // 如果用户手机没装邮件App，则弹出Toast提示
                                    Toast.makeText(context, "请发送邮件至: 3242752034@qq.com", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("联系作者", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

// 列表项小组件
@Composable
fun RequirementItem(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HelpScreenPreview() {
    HelpScreen()
}
