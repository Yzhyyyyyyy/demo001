package com.example.demo001

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// --- 色彩体系 ---
val DfBgDark = Color(0xFF0A0F16)     // 极深战术蓝黑
val DfSurface = Color(0xFF141D26)    // 表面深灰
val DfBorder = Color(0xFF2A3A4C)     // 科技边框灰
val DfNeonGreen = Color(0xFF00E676)  // 霓虹战术绿 (主色)
val DfTextGray = Color(0xFF8B9EB7)   // 辅助文字灰
val DfLegendary = Color(0xFFFF3D00)  // 极品红/橙红
val DfEpic = Color(0xFFAA00FF)       // 史诗紫
val DfRare = Color(0xFF00B0FF)       // 稀有蓝

enum class DfTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Decode("破译", Icons.Filled.Lock),
    Recharge("充值", Icons.Filled.AddCircle),
    Exchange("军需官", Icons.Filled.ShoppingCart),
    Arsenal("军械库", Icons.AutoMirrored.Filled.List)
}

// 战术网格背景
@Composable
fun DfTacticalBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(DfBgDark)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF112233), Color.Transparent), center = Offset(size.width / 2, size.height * 0.4f), radius = size.width))
            val gridSize = 60.dp.toPx()
            for (x in 0..(size.width / gridSize).toInt()) {
                drawLine(color = DfBorder.copy(alpha = 0.3f), start = Offset(x * gridSize, 0f), end = Offset(x * gridSize, size.height), strokeWidth = 1f)
            }
            for (y in 0..(size.height / gridSize).toInt()) {
                drawLine(color = DfBorder.copy(alpha = 0.3f), start = Offset(0f, y * gridSize), end = Offset(size.width, y * gridSize), strokeWidth = 1f)
            }
        }
        content()
    }
}

@Composable
fun DeltaForceScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var deltaCoins by remember { mutableIntStateOf(1000) }
    var quantumShards by remember { mutableIntStateOf(0) }
    val inventory = remember { mutableStateMapOf<String, Int>() }
    var currentTab by remember { mutableStateOf(DfTab.Decode) }
    var pityCount by remember { mutableIntStateOf(0) }
    var showResultDialog by remember { mutableStateOf<List<Pair<String, Color>>?>(null) }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = DfSurface, tonalElevation = 0.dp,
                modifier = Modifier.border(1.dp, DfBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                enumValues<DfTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab, onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = DfBgDark, selectedTextColor = DfNeonGreen, indicatorColor = DfNeonGreen, unselectedIconColor = DfTextGray, unselectedTextColor = DfTextGray)
                    )
                }
            }
        }
    ) { paddingValues ->
        DfTacticalBackground {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // --- 顶部战术状态栏 ---
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 20.dp, end = 20.dp, bottom = 16.dp).align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(DfSurface, RoundedCornerShape(8.dp)).border(1.dp, DfBorder, RoundedCornerShape(8.dp)).clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = DfNeonGreen) }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.background(DfSurface, RoundedCornerShape(4.dp)).border(1.dp, DfBorder, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💾", fontSize = 14.sp); Spacer(Modifier.width(6.dp)); Text("$quantumShards", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Row(modifier = Modifier.background(DfSurface, RoundedCornerShape(4.dp)).border(1.dp, DfBorder, RoundedCornerShape(4.dp)).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("💳", fontSize = 14.sp); Spacer(Modifier.width(6.dp)); Text("$deltaCoins", fontSize = 14.sp, fontWeight = FontWeight.Black, color = DfNeonGreen)
                        }
                    }
                }

                // 抽卡结果弹窗 (修复了 Bug)
                if (showResultDialog != null) {
                    AlertDialog(
                        onDismissRequest = { showResultDialog = null },
                        modifier = Modifier.border(1.dp, DfBorder, RoundedCornerShape(8.dp)), // 修复1：将 border 移到 modifier 中
                        title = { Text("MANDELBRICK DECODED // 破译完成", fontWeight = FontWeight.Black, color = DfNeonGreen, letterSpacing = 1.sp, fontSize = 18.sp) },
                        text = {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.heightIn(max = 300.dp), // 修复2：给 LazyVerticalGrid 增加最大高度限制，防止崩溃
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(showResultDialog!!) { (name, color) ->
                                    Box(modifier = Modifier.fillMaxWidth().background(DfSurface, RoundedCornerShape(4.dp)).border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(name, color = color, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showResultDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = DfNeonGreen, contentColor = DfBgDark), shape = RoundedCornerShape(4.dp)) { Text("确认 (ACK)", fontWeight = FontWeight.Bold) }
                        },
                        containerColor = DfBgDark,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                // 内容区
                Box(modifier = Modifier.fillMaxSize().padding(top = 90.dp, start = 20.dp, end = 20.dp)) {
                    when (currentTab) {
                        DfTab.Decode -> DfDecodeTab(
                            coins = deltaCoins, pityCount = pityCount,
                            onDraw = { times, cost ->
                                if (deltaCoins < cost) { Toast.makeText(context, "三角洲币不足", Toast.LENGTH_SHORT).show(); return@DfDecodeTab }
                                deltaCoins -= cost
                                val results = mutableListOf<Pair<String, Color>>()

                                // 皮肤池定义
                                val redSkins = listOf("【红品】干员-威龙(猩红)", "【红品】M4A1-地狱火", "【红品】干员-骇客(矩阵)")
                                val purpleSkins = listOf("【紫品】AKM-暗夜紫晶", "【紫品】P90-霓虹", "【紫品】干员-幻影", "【紫品】ASVAL-毒刺")
                                val blueSkins = listOf("【蓝品】G18-冰川", "【蓝品】战术背包-极地", "【蓝品】军用匕首-碳钢", "【蓝品】基础迷彩服")

                                repeat(times) {
                                    pityCount++
                                    val roll = Random.nextFloat()
                                    when {
                                        pityCount >= 80 || roll < 0.015f -> { results.add(redSkins.random() to DfLegendary); pityCount = 0 }
                                        roll < 0.15f -> results.add(purpleSkins.random() to DfEpic)
                                        roll < 0.45f -> results.add(blueSkins.random() to DfRare)
                                        else -> { results.add("量子碎片 * 10" to DfTextGray); quantumShards += 10 }
                                    }
                                }
                                results.forEach { inventory[it.first] = (inventory[it.first] ?: 0) + 1 }
                                showResultDialog = results
                            }
                        )
                        DfTab.Recharge -> DfRechargeTab { amount -> deltaCoins += amount }
                        DfTab.Exchange -> DfExchangeTab(quantumShards, { quantumShards = it }, { inventory[it] = (inventory[it] ?: 0) + 1 })
                        DfTab.Arsenal -> DfInventoryTab(inventory)
                    }
                }
            }
        }
    }
}

// ==================== 1. 曼德尔砖破译 (抽卡界面) ====================
@Composable
fun DfDecodeTab(coins: Int, pityCount: Int, onDraw: (Int, Int) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var isDecoding by remember { mutableStateOf(false) }

    val coreRotation = remember { Animatable(0f) }
    val coreScale = remember { Animatable(1f) }

    fun executeDecode(times: Int, cost: Int) {
        if (coins < cost) { onDraw(times, cost); return }
        coroutineScope.launch {
            isDecoding = true
            launch { coreScale.animateTo(0.8f, tween(500)) }
            coreRotation.animateTo(coreRotation.value + 1080f, tween(1500, easing = FastOutSlowInEasing))
            launch { coreScale.animateTo(1.1f, tween(200)); coreScale.animateTo(1f, tween(200)) }
            delay(400)
            onDraw(times, cost)
            isDecoding = false
        }
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().background(DfSurface, RoundedCornerShape(8.dp)).border(1.dp, DfBorder, RoundedCornerShape(8.dp)).padding(16.dp)) {
            Column {
                Text("MANDELBRICK DECRYPTION", color = DfTextGray, fontSize = 12.sp, letterSpacing = 4.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("曼德尔砖破译", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = { pityCount / 80f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = DfLegendary, trackColor = DfBgDark)
                Spacer(modifier = Modifier.height(4.dp))
                Text("距离保底必出【红品】皮肤还剩: ${80 - pityCount} 次", color = DfLegendary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(280.dp).scale(coreScale.value).rotate(coreRotation.value)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.width / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawArc(color = DfBorder, startAngle = 0f, sweepAngle = 100f, useCenter = false, style = Stroke(width = 4.dp.toPx()))
                    drawArc(color = DfNeonGreen, startAngle = 120f, sweepAngle = 80f, useCenter = false, style = Stroke(width = 4.dp.toPx()))
                    drawArc(color = DfBorder, startAngle = 220f, sweepAngle = 120f, useCenter = false, style = Stroke(width = 4.dp.toPx()))

                    val hexPath = Path()
                    for (i in 0..5) {
                        val angle = (i * 60f - 30f) * (Math.PI / 180f).toFloat()
                        val x = center.x + (radius * 0.6f) * cos(angle)
                        val y = center.y + (radius * 0.6f) * sin(angle)
                        if (i == 0) hexPath.moveTo(x, y) else hexPath.lineTo(x, y)
                    }
                    hexPath.close()
                    drawPath(path = hexPath, color = DfSurface)
                    drawPath(path = hexPath, color = DfNeonGreen, style = Stroke(width = 2.dp.toPx()))

                    drawCircle(brush = Brush.radialGradient(listOf(DfNeonGreen.copy(alpha = 0.8f), Color.Transparent)), radius = radius * 0.4f)
                }
            }

            if (isDecoding) {
                Text("曼德尔砖破译中...", color = DfNeonGreen, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
            } else {
                Text("READY", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 4.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = { executeDecode(1, 100) }, enabled = !isDecoding, modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = DfSurface, contentColor = Color.White), border = BorderStroke(1.dp, DfBorder)
            ) { Text("单次破译\n100 币", textAlign = TextAlign.Center, fontWeight = FontWeight.Bold) }

            Button(
                onClick = { executeDecode(10, 1000) }, enabled = !isDecoding, modifier = Modifier.weight(1f).height(64.dp),
                shape = RoundedCornerShape(4.dp), colors = ButtonDefaults.buttonColors(containerColor = DfNeonGreen, contentColor = DfBgDark)
            ) { Text("十次破译\n1000 币", textAlign = TextAlign.Center, fontWeight = FontWeight.Black) }
        }
    }
}

// ==================== 2. 充值中心 ====================
@Composable
fun DfRechargeTab(onRecharge: (Int) -> Unit) {
    val context = LocalContext.current
    val rechargeOptions = listOf(6 to 60, 30 to 300, 68 to 680, 128 to 1280, 198 to 1980, 328 to 3280, 648 to 6480)
    val chunkedOptions = rechargeOptions.chunked(3)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("资金补充", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
        Text("FUNDS RECHARGE", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DfNeonGreen, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            chunkedOptions.forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { (price, amount) ->
                        Button(
                            onClick = { onRecharge(amount); Toast.makeText(context, "资金已到账：$amount 币", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (price == 648) DfNeonGreen.copy(alpha = 0.1f) else DfSurface, contentColor = if (price == 648) DfNeonGreen else Color.White),
                            border = BorderStroke(1.dp, if (price == 648) DfNeonGreen else DfBorder)
                        ) { Text("¥$price\n$amount 币", textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    }
                    if (rowItems.size < 3) { repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) } }
                }
            }
        }
    }
}

// ==================== 3. 军需官 (兑换商店) ====================
@Composable
fun DfExchangeTab(shards: Int, onShardsChange: (Int) -> Unit, onAddItem: (String) -> Unit) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("黑市军需官", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
        Text("BLACK MARKET", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DfNeonGreen, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).background(DfSurface, RoundedCornerShape(4.dp)).border(1.dp, DfLegendary.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("【红品】干员-威龙(猩红)", fontWeight = FontWeight.Black, color = DfLegendary, fontSize = 16.sp)
                Text("极品特战皮肤", color = DfTextGray, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    if (shards >= 3000) { onShardsChange(shards - 3000); onAddItem("【红品】干员-威龙(猩红)"); Toast.makeText(context, "交易成功", Toast.LENGTH_SHORT).show() }
                    else Toast.makeText(context, "碎片不足", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DfLegendary, contentColor = Color.White), shape = RoundedCornerShape(4.dp)
            ) { Text("3000 碎片", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().background(DfSurface, RoundedCornerShape(4.dp)).border(1.dp, DfEpic.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("【紫品】AKM-暗夜紫晶", fontWeight = FontWeight.Black, color = DfEpic, fontSize = 16.sp)
                Text("史诗武器皮肤", color = DfTextGray, fontSize = 12.sp)
            }
            Button(
                onClick = {
                    if (shards >= 1000) { onShardsChange(shards - 1000); onAddItem("【紫品】AKM-暗夜紫晶"); Toast.makeText(context, "交易成功", Toast.LENGTH_SHORT).show() }
                    else Toast.makeText(context, "碎片不足", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DfSurface, contentColor = Color.White), border = BorderStroke(1.dp, DfBorder), shape = RoundedCornerShape(4.dp)
            ) { Text("1000 碎片", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ==================== 4. 军械库 (仓库) ====================
@Composable
fun DfInventoryTab(inventory: Map<String, Int>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("个人军械库", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp)
        Text("PERSONAL ARSENAL", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DfNeonGreen, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(24.dp))

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("军械库当前为空\nNO DATA FOUND", color = DfBorder, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, fontSize = 18.sp)
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(inventory.toList()) { (name, count) ->
                    val color = when {
                        name.contains("红品") -> DfLegendary
                        name.contains("紫品") -> DfEpic
                        name.contains("蓝品") -> DfRare
                        else -> DfTextGray
                    }
                    Column(
                        modifier = Modifier.background(DfSurface, RoundedCornerShape(4.dp)).border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(name, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("数量: $count", color = DfTextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeltaForceScreenPreview() {
    DeltaForceScreen(onBackClick = {})
}