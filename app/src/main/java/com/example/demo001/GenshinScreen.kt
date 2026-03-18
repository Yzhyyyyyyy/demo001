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

// --- 颜色定义 ---
val GenshinGold = Color(0xFFD4AF37)
val GenshinPurple = Color(0xFF9370DB)
val GenshinBlue = Color(0xFF6495ED)
val GenshinTextDark = Color(0xFF0F172A)

// 页面枚举
enum class GenshinTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Wish("祈愿", Icons.Filled.Star),
    Inventory("背包", Icons.AutoMirrored.Filled.List),
    Recharge("充值", Icons.Filled.ShoppingCart),
    Settings("设置", Icons.Filled.Settings)
}

// 池子枚举：原神特色 = 角色池 + 武器池
enum class GenshinPoolType(val title: String) {
    Character("限定角色祈愿"),
    Weapon("限定武器祈愿")
}

// 充值（原石）
data class GenshinTopUpOption(val price: Int, val primogem: Int)
val genshinTopUpList = listOf(
    GenshinTopUpOption(6, 60),
    GenshinTopUpOption(30, 300),
    GenshinTopUpOption(98, 980),
    GenshinTopUpOption(198, 1980),
    GenshinTopUpOption(328, 3280),
    GenshinTopUpOption(648, 6480)
)

@Composable
fun GenshinScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    var currentTab by remember { mutableStateOf(GenshinTab.Wish) }
    var primogem by remember { mutableIntStateOf(1760) }
    var currentPool by remember { mutableStateOf(GenshinPoolType.Character) }

    // 角色池保底
    var charPity by remember { mutableIntStateOf(0) }
    var charGuaranteed by remember { mutableStateOf(false) }

    // 武器池保底 + 命定值（0~2）
    var weaponPity by remember { mutableIntStateOf(0) }
    var weaponGuaranteed by remember { mutableStateOf(false) }
    var epitomizedPath by remember { mutableIntStateOf(0) }

    val inventory = remember { mutableStateMapOf<String, Int>() }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                enumValues<GenshinTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = GenshinTextDark,
                            indicatorColor = GenshinTextDark,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
                .padding(paddingValues)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFE082).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.8f, size.height * 0.1f),
                        radius = size.width * 0.7f
                    )
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF80DEEA).copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(size.width * 0.2f, size.height * 0.8f),
                        radius = size.width * 0.7f
                    )
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = GenshinTextDark)
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✦", color = GenshinGold, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$primogem", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GenshinTextDark)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    when (currentTab) {
                        GenshinTab.Wish -> GenshinWishTab(
                            primogem = primogem,
                            onGemChange = { primogem = it },
                            poolType = currentPool,
                            pity = if (currentPool == GenshinPoolType.Character) charPity else weaponPity,
                            isGuaranteed = if (currentPool == GenshinPoolType.Character) charGuaranteed else weaponGuaranteed,
                            epitomizedPath = epitomizedPath,
                            onUpdatePity = { newPity, newGuaranteed ->
                                if (currentPool == GenshinPoolType.Character) {
                                    charPity = newPity
                                    charGuaranteed = newGuaranteed
                                } else {
                                    weaponPity = newPity
                                    weaponGuaranteed = newGuaranteed
                                }
                            },
                            onUpdateEpitomized = { epitomizedPath = it },
                            onItemPulled = { item -> inventory[item] = (inventory[item] ?: 0) + 1 },
                            onGoToRecharge = { currentTab = GenshinTab.Recharge }
                        )
                        GenshinTab.Inventory -> GenshinInventoryTab(inventory) { inventory.clear() }
                        GenshinTab.Recharge -> GenshinRechargeTab { amount ->
                            primogem += amount
                            Toast.makeText(context, "成功充值 $amount 原石！", Toast.LENGTH_SHORT).show()
                        }
                        GenshinTab.Settings -> GenshinSettingsTab(
                            currentPool = currentPool,
                            onPoolChange = { currentPool = it }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenshinWishTab(
    primogem: Int,
    onGemChange: (Int) -> Unit,
    poolType: GenshinPoolType,
    pity: Int,
    isGuaranteed: Boolean,
    epitomizedPath: Int,
    onUpdatePity: (Int, Boolean) -> Unit,
    onUpdateEpitomized: (Int) -> Unit,
    onItemPulled: (String) -> Unit,
    onGoToRecharge: () -> Unit
) {
    var isPulling by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var currentResults by remember { mutableStateOf<List<GenshinPullResult>>(emptyList()) }
    var maxRarityPulled by remember { mutableIntStateOf(3) }
    var showRechargePrompt by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun doPulls(times: Int) {
        if (primogem < times * 160) {
            showRechargePrompt = true
            return
        }

        onGemChange(primogem - times * 160)
        isPulling = true

        coroutineScope.launch {
            val results = mutableListOf<GenshinPullResult>()
            var tempPity = pity
            var tempGuaranteed = isGuaranteed
            var tempEpitomized = epitomizedPath

            for (i in 0 until times) {
                tempPity += 1

                val hardPity = if (poolType == GenshinPoolType.Character) 90 else 80
                val softPity = if (poolType == GenshinPoolType.Character) 74 else 65
                val baseProb = if (poolType == GenshinPoolType.Character) 0.006f else 0.007f

                val prob5 = if (tempPity <= softPity) baseProb else baseProb + 0.06f * (tempPity - softPity)
                val is5Star = Random.nextFloat() < prob5 || tempPity >= hardPity

                val result: GenshinPullResult
                if (is5Star) {
                    tempPity = 0
                    if (poolType == GenshinPoolType.Character) {
                        // 角色池 50/50
                        if (tempGuaranteed) {
                            tempGuaranteed = false
                            result = GenshinPullResult("限定五星角色", 5, GenshinGold)
                        } else {
                            if (Random.nextFloat() < 0.5f) {
                                tempGuaranteed = false
                                result = GenshinPullResult("限定五星角色", 5, GenshinGold)
                            } else {
                                tempGuaranteed = true
                                result = GenshinPullResult("常驻五星角色", 5, GenshinGold)
                            }
                        }
                    } else {
                        // 武器池 + 命定值（简化但有特色）
                        if (tempEpitomized >= 2) {
                            tempEpitomized = 0
                            tempGuaranteed = false
                            result = GenshinPullResult("指定五星武器", 5, GenshinGold)
                        } else if (tempGuaranteed) {
                            tempGuaranteed = false
                            if (Random.nextFloat() < 0.5f) {
                                tempEpitomized = 0
                                result = GenshinPullResult("指定五星武器", 5, GenshinGold)
                            } else {
                                tempEpitomized = minOf(tempEpitomized + 1, 2)
                                result = GenshinPullResult("限定五星武器", 5, GenshinGold)
                            }
                        } else {
                            if (Random.nextFloat() < 0.75f) {
                                if (Random.nextFloat() < 0.5f) {
                                    tempEpitomized = 0
                                    result = GenshinPullResult("指定五星武器", 5, GenshinGold)
                                } else {
                                    tempEpitomized = minOf(tempEpitomized + 1, 2)
                                    result = GenshinPullResult("限定五星武器", 5, GenshinGold)
                                }
                            } else {
                                tempGuaranteed = true
                                result = GenshinPullResult("常驻五星武器", 5, GenshinGold)
                            }
                        }
                    }
                } else if (Random.nextFloat() < 0.1f) {
                    result = GenshinPullResult("四星", 4, GenshinPurple)
                } else {
                    result = GenshinPullResult("三星", 3, GenshinBlue)
                }

                onItemPulled(result.name)
                results.add(result)
            }

            onUpdatePity(tempPity, tempGuaranteed)
            onUpdateEpitomized(tempEpitomized)

            currentResults = results.sortedByDescending { it.rarity }
            maxRarityPulled = currentResults.maxOf { it.rarity }

            delay(2800)
            isPulling = false
            showResult = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(poolType.title, color = GenshinTextDark, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("$pity", fontSize = 64.sp, fontWeight = FontWeight.Black, color = GenshinTextDark)
                    Text("当前已祈愿", fontSize = 14.sp, color = Color(0xFF94A3B8))

                    if (poolType == GenshinPoolType.Weapon) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("命定值：$epitomizedPath / 2", fontSize = 13.sp, color = GenshinGold, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { doPulls(1) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GenshinTextDark),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("祈愿 1 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = GenshinGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("160", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Button(
                    onClick = { doPulls(10) },
                    modifier = Modifier.weight(1f).height(72.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GenshinTextDark, contentColor = Color.White),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("祈愿 10 次", fontWeight = FontWeight.Black, fontSize = 20.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✦", color = GenshinGold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("1600", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (isPulling) GenshinGachaAnimationOverlay(maxRarity = maxRarityPulled)

        if (showResult) {
            AlertDialog(
                onDismissRequest = { showResult = false },
                containerColor = Color.White,
                title = { Text("祈愿结果", color = GenshinTextDark, fontWeight = FontWeight.Bold) },
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
                                Text(
                                    result.name,
                                    color = result.color,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showResult = false }) {
                        Text("确定", color = GenshinTextDark, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        if (showRechargePrompt) {
            AlertDialog(
                onDismissRequest = { showRechargePrompt = false },
                title = { Text("原石不足", fontWeight = FontWeight.Bold, color = GenshinTextDark) },
                text = { Text("您的原石余额不足，是否前往充值获取？", fontSize = 16.sp) },
                confirmButton = {
                    Button(
                        onClick = { showRechargePrompt = false; onGoToRecharge() },
                        colors = ButtonDefaults.buttonColors(containerColor = GenshinTextDark)
                    ) { Text("去充值", fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showRechargePrompt = false }) {
                        Text("取消", color = Color(0xFF64748B))
                    }
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun GenshinGachaAnimationOverlay(maxRarity: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "genshin_gacha")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    var currentColor by remember { mutableStateOf(GenshinBlue) }

    LaunchedEffect(maxRarity) {
        delay(1200)
        currentColor = when (maxRarity) {
            5 -> GenshinGold
            4 -> GenshinPurple
            else -> GenshinBlue
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(600),
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
            text = if (currentColor == GenshinGold) "✨ 神明的眷顾降临！✨" else "星辉祈愿中...",
            color = if (currentColor == GenshinGold) GenshinGold else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 150.dp)
        )
    }
}

@Composable
fun GenshinInventoryTab(inventory: Map<String, Int>, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("我的背包", color = GenshinTextDark, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) {
                Text("清空背包", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
            }
        }

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("背包空空如也，快去祈愿吧！", color = Color(0xFF94A3B8))
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
                        name.contains("五星") -> GenshinGold
                        name.contains("四星") -> GenshinPurple
                        else -> GenshinBlue
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
                            Text("拥有: $count", color = GenshinTextDark, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GenshinRechargeTab(onRecharge: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("充值中心", fontSize = 24.sp, fontWeight = FontWeight.Black, color = GenshinTextDark, modifier = Modifier.padding(vertical = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(genshinTopUpList) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clickable { onRecharge(option.primogem) },
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
                            Text("✦", color = GenshinGold, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${option.primogem}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = GenshinTextDark)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .background(GenshinTextDark, RoundedCornerShape(8.dp))
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

@Composable
fun GenshinSettingsTab(currentPool: GenshinPoolType, onPoolChange: (GenshinPoolType) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("卡池设置", color = GenshinTextDark, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("选择当前祈愿目标：", color = Color(0xFF64748B), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(
                        onClick = { onPoolChange(GenshinPoolType.Character) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == GenshinPoolType.Character) GenshinTextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == GenshinPoolType.Character) Color.White else Color(0xFF64748B)
                        )
                    ) { Text("限定角色池", fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = { onPoolChange(GenshinPoolType.Weapon) },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentPool == GenshinPoolType.Weapon) GenshinTextDark else Color(0xFFE2E8F0),
                            contentColor = if (currentPool == GenshinPoolType.Weapon) Color.White else Color(0xFF64748B)
                        )
                    ) { Text("限定武器池", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

data class GenshinPullResult(val name: String, val rarity: Int, val color: Color)

@Preview(showBackground = true)
@Composable
fun GenshinScreenPreview() {
    GenshinScreen(onBackClick = {})
}
