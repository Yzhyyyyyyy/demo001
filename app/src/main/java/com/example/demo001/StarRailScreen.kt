package com.example.demo001

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- 颜色定义 (适配浅色背景) ---
val RarityGold = Color(0xFFD4AF37)
val RarityPurple = Color(0xFF9370DB)
val RarityBlue = Color(0xFF6495ED)
val TextDark = Color(0xFF0F172A)

// 页面枚举
enum class StarRailTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Warp("跃迁", Icons.Filled.Star),
    Inventory("背包", Icons.AutoMirrored.Filled.List),
    Recharge("充值", Icons.Filled.ShoppingCart),
    Settings("设置", Icons.Filled.Settings)
}

// 池子枚举
enum class PoolType(val title: String) { Character("限定角色跃迁"), LightCone("限定光锥跃迁") }

// 充值选项数据模型
data class TopUpOption(val price: Int, val jade: Int)
val topUpList = listOf(
    TopUpOption(6, 60), TopUpOption(30, 300),
    TopUpOption(98, 980), TopUpOption(198, 1980),
    TopUpOption(328, 3280), TopUpOption(648, 6480)
)

@Composable
fun StarRailScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    // --- 全局状态 ---
    var currentTab by remember { mutableStateOf(StarRailTab.Warp) }
    // 初始星琼：刚好够一发单抽(160) + 一发十连(1600) = 1760
    var stellarJade by remember { mutableIntStateOf(1760) }
    var currentPool by remember { mutableStateOf(PoolType.Character) }

    // 抽卡保底数据
    var charPity by remember { mutableIntStateOf(0) }
    var charGuaranteed by remember { mutableStateOf(false) }
    var weaponPity by remember { mutableIntStateOf(0) }
    var weaponGuaranteed by remember { mutableStateOf(false) }

    // 背包数据
    val inventory = remember { mutableStateMapOf<String, Int>() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                enumValues<StarRailTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, selectedTextColor = TextDark,
                            indicatorColor = TextDark, unselectedIconColor = Color(0xFF94A3B8), unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC)) // 浅色背景底色
                .padding(paddingValues)
        ) {
            // ================= 核心背景：蓝紫圆形渐变光晕 =================
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
                // 顶部状态栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = TextDark)
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✦", color = RarityGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$stellarJade", fontSize = 16.sp, fontWeight = FontWeight.Black, color = TextDark)
                        }
                    }
                }

                // 内容区域切换
                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    when (currentTab) {
                        StarRailTab.Warp -> WarpTab(
                            stellarJade = stellarJade,
                            onJadeChange = { stellarJade = it },
                            poolType = currentPool,
                            pity = if (currentPool == PoolType.Character) charPity else weaponPity,
                            isGuaranteed = if (currentPool == PoolType.Character) charGuaranteed else weaponGuaranteed,
                            onUpdatePity = { newPity, newGuaranteed ->
                                if (currentPool == PoolType.Character) {
                                    charPity = newPity
                                    charGuaranteed = newGuaranteed
                                } else {
                                    weaponPity = newPity
                                    weaponGuaranteed = newGuaranteed
                                }
                            },
                            onItemPulled = { item -> inventory[item] = (inventory[item] ?: 0) + 1 },
                            onGoToRecharge = { currentTab = StarRailTab.Recharge }
                        )
                        StarRailTab.Inventory -> InventoryTab(inventory) { inventory.clear() }
                        StarRailTab.Recharge -> RechargeTab { amount ->
                            stellarJade += amount
                            Toast.makeText(context, "成功充值 $amount 星琼！", Toast.LENGTH_SHORT).show()
                        }
                        StarRailTab.Settings -> SettingsTab(
                            currentPool = currentPool,
                            onPoolChange = { currentPool = it }
                        )
                    }
                }
            }
        }
    }
}

// ==================== 1. 抽卡页面 (WarpTab) ====================
@Composable
fun WarpTab(
    stellarJade: Int,
    onJadeChange: (Int) -> Unit,
    poolType: PoolType,
    pity: Int,
    isGuaranteed: Boolean,
    onUpdatePity: (Int, Boolean) -> Unit,
    onItemPulled: (String) -> Unit,
    onGoToRecharge: () -> Unit
) {
    var isPulling by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var currentResults by remember { mutableStateOf<List<PullResult>>(emptyList()) }
    var maxRarityPulled by remember { mutableIntStateOf(3) }
    var showRechargePrompt by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // 核心抽卡逻辑
    fun doPulls(times: Int) {
        if (stellarJade < times * 160) {
            showRechargePrompt = true
            return
        }

        onJadeChange(stellarJade - times * 160)
        isPulling = true

        coroutineScope.launch {
            val results = mutableListOf<PullResult>()

            var tempPity = pity
            var tempGuaranteed = isGuaranteed

            for (i in 0 until times) {
                tempPity += 1

                val hardPity = if (poolType == PoolType.Character) 90 else 80
                val softPity = if (poolType == PoolType.Character) 73 else 65
                val baseProb = if (poolType == PoolType.Character) 0.006f else 0.008f

                val prob5 = if (tempPity <= softPity) baseProb else baseProb + 0.06f * (tempPity - softPity)
                val is5Star = Random.nextFloat() < prob5 || tempPity >= hardPity

                val result: PullResult
                if (is5Star) {
                    tempPity = 0 // 真实机制：只要出金，垫的次数立刻清零！

                    if (tempGuaranteed) {
                        // 触发大保底：必定是限定五星
                        tempGuaranteed = false
                        result = PullResult("限定五星", 5, RarityGold)
                    } else {
                        // 小保底：50% 概率不歪 (光锥池是 75%)
                        val winRate = if (poolType == PoolType.Character) 0.5f else 0.75f
                        if (Random.nextFloat() < winRate) {
                            tempGuaranteed = false // 没歪
                            result = PullResult("限定五星", 5, RarityGold)
                        } else {
                            tempGuaranteed = true // 歪了！下次必定大保底
                            result = PullResult("常驻五星", 5, RarityGold)
                        }
                    }
                } else if (Random.nextFloat() < 0.1f) { // 10% 概率出四星
                    result = PullResult("四星", 4, RarityPurple)
                } else {
                    result = PullResult("三星", 3, RarityBlue)
                }

                onItemPulled(result.name)
                results.add(result)
            }

            // 循环结束后，更新保底次数和大保底状态
            onUpdatePity(tempPity, tempGuaranteed)

            currentResults = results.sortedByDescending { it.rarity }
            maxRarityPulled = currentResults.maxOf { it.rarity }

            delay(2800) // 动画总时长稍微加长一点，给变色留足时间
            isPulling = false
            showResult = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 卡池信息面板
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(poolType.title, color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("$pity", fontSize = 64.sp, fontWeight = FontWeight.Black, color = TextDark)
                    Text("当前已抽", fontSize = 14.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 抽卡按钮
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { doPulls(1) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = TextDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("跃迁 1 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = RarityGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("160", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Button(
                    onClick = { doPulls(10) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextDark, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("跃迁 10 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = RarityGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1600", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 抽卡动画遮罩 ---
        if (isPulling) {
            GachaAnimationOverlay(maxRarity = maxRarityPulled)
        }

        // 抽卡结果弹窗
        if (showResult) {
            AlertDialog(
                onDismissRequest = { showResult = false },
                containerColor = Color.White,
                title = { Text("跃迁结果", color = TextDark, fontWeight = FontWeight.Bold) },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentResults) { result ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(result.color.copy(alpha = 0.1f))
                                    .border(1.dp, result.color, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(result.name, color = result.color, fontSize = 14.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showResult = false }) {
                        Text("确定", color = TextDark, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // 余额不足弹窗
        if (showRechargePrompt) {
            AlertDialog(
                onDismissRequest = { showRechargePrompt = false },
                title = { Text("星琼不足", fontWeight = FontWeight.Bold, color = TextDark) },
                text = { Text("您的星琼余额不足，是否前往充值获取？", fontSize = 16.sp) },
                confirmButton = {
                    Button(
                        onClick = { showRechargePrompt = false; onGoToRecharge() },
                        colors = ButtonDefaults.buttonColors(containerColor = TextDark)
                    ) {
                        Text("去充值", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRechargePrompt = false }) { Text("取消", color = Color(0xFF64748B)) }
                },
                containerColor = Color.White
            )
        }
    }
}

// --- 抽卡动画组件 (带期待感升格变色) ---
@Composable
fun GachaAnimationOverlay(maxRarity: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "gacha")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 2.0f,
        animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "scale"
    )

    // 初始颜色永远是蓝色
    var currentColor by remember { mutableStateOf(RarityBlue) }

    // 延迟 1.2 秒后，如果是紫或金，则发生颜色突变 (升格)
    LaunchedEffect(maxRarity) {
        delay(1200)
        currentColor = when (maxRarity) {
            5 -> RarityGold
            4 -> RarityPurple
            else -> RarityBlue
        }
    }

    // 颜色渐变动画
    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(600), // 变色过程耗时 0.6 秒
        label = "color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Brush.radialGradient(colors = listOf(animatedColor, Color.Transparent)))
        )
        Text(
            text = if (currentColor == RarityGold) "✨ 发现五星信号！✨" else "列车跃迁中...",
            color = if (currentColor == RarityGold) RarityGold else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 150.dp)
        )
    }
}

// ==================== 2. 背包页面 (InventoryTab) ====================
@Composable
fun InventoryTab(inventory: Map<String, Int>, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("我的背包", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) { Text("清空背包", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
        }

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("背包空空如也，快去跃迁吧！", color = Color(0xFF94A3B8))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                items(inventory.toList().sortedByDescending {
                    when {
                        it.first.contains("五星") -> 5
                        it.first.contains("四星") -> 4
                        else -> 3
                    }
                }) { (name, count) ->
                    val color = when {
                        name.contains("五星") -> RarityGold
                        name.contains("四星") -> RarityPurple
                        else -> RarityBlue
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, color),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, color = color, fontSize = 16.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("拥有: $count", color = TextDark, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 3. 充值页面 (RechargeTab) ====================
@Composable
fun RechargeTab(onRecharge: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("充值中心", fontSize = 24.sp, fontWeight = FontWeight.Black, color = TextDark, modifier = Modifier.padding(vertical = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(topUpList) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { onRecharge(option.jade) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = RarityGold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${option.jade}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(TextDark, RoundedCornerShape(8.dp))
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        ) {
                            Text("¥ ${option.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ==================== 4. 设置页面 (SettingsTab) ====================
@Composable
fun SettingsTab(currentPool: PoolType, onPoolChange: (PoolType) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("卡池设置", color = TextDark, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("选择当前跃迁目标：", color = Color(0xFF64748B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(
                        onClick = { onPoolChange(PoolType.Character) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == PoolType.Character) TextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == PoolType.Character) Color.White else Color(0xFF64748B)
                        )
                    ) {
                        Text("限定角色池", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onPoolChange(PoolType.LightCone) },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == PoolType.LightCone) TextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == PoolType.LightCone) Color.White else Color(0xFF64748B)
                        )
                    ) {
                        Text("限定光锥池", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 数据类
data class PullResult(val name: String, val rarity: Int, val color: Color)

@Preview(showBackground = true)
@Composable
fun StarRailScreenPreview() {
    StarRailScreen(onBackClick = {})
}
