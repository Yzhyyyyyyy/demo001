package com.example.demo001

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// ================= 1. 数据模型 =================

enum class CardRarity(val label: String, val color: Color, val textColor: Color) {
    EPIC("Epic", Color(0xFFD8B4FE), Color(0xFF4C1D95)),         // 紫色
    SHOWTIME("ShowTime", Color(0xFF86EFAC), Color(0xFF14532D)), // 绿色
    BIGTIME("BigTime", Color(0xFFFCA5A5), Color(0xFF7F1D1D)),   // 红色
    FEATURED("精选", Color(0xFF7DD3FC), Color(0xFF0C4A6E)),       // 蓝色
    NORMAL("普卡", Color(0xFFE2E8F0), Color(0xFF334155))          // 灰色
}

data class CardInstance(val rarity: CardRarity, val number: Int? = null)

enum class EfootballTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Gacha("抽球", Icons.Filled.PlayArrow),
    Inventory("背包", Icons.Filled.Person),
    Recharge("充值", Icons.Filled.ShoppingCart),
    Settings("设置", Icons.Filled.Settings)
}

// 抽卡特效的三个档次
enum class DrawTier { POOR, GOOD, EXCELLENT }

// 充值选项的数据模型
data class RechargeOption(val price: Int, val coins: Int, val bonus: Int = 0)

val rechargeList = listOf(
    RechargeOption(6, 60),
    RechargeOption(30, 300, 30),
    RechargeOption(68, 680, 70),
    RechargeOption(128, 1280, 150),
    RechargeOption(328, 3280, 400),
    RechargeOption(648, 6480, 1000)
)

// ================= 2. 精美的金币图标组件 =================
@Composable
fun GoldCoinIcon(modifier: Modifier = Modifier, size: Int = 24) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFF5A623)) // 黄金渐变
                )
            )
            .border(1.dp, Color(0xFFB8860B), CircleShape), // 暗金色描边
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            color = Color.White,
            fontSize = (size * 0.6).sp,
            fontWeight = FontWeight.Black
        )
    }
}

// ================= 3. 主页面 =================

@Composable
fun EfootballMainScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var coins by remember { mutableStateOf(1000) }
    var currentTab by remember { mutableStateOf(EfootballTab.Gacha) }

    var settingEpic by remember { mutableStateOf(1) }
    var settingShowTime by remember { mutableStateOf(1) }
    var settingBigTime by remember { mutableStateOf(1) }

    val goodCardsCount = settingEpic + settingShowTime + settingBigTime
    val settingFeatured = goodCardsCount * 4
    val settingNormal = goodCardsCount * 45
    val settingTotal = goodCardsCount + settingFeatured + settingNormal

    var epicPool by remember { mutableStateOf((1..settingEpic).toList()) }
    var showTimePool by remember { mutableStateOf((1..settingShowTime).toList()) }
    var bigTimePool by remember { mutableStateOf((1..settingBigTime).toList()) }
    var featuredPool by remember { mutableStateOf((1..settingFeatured).toList()) }
    var remainNormal by remember { mutableStateOf(settingNormal) }

    val remainTotal = epicPool.size + showTimePool.size + bigTimePool.size + featuredPool.size + remainNormal

    val inventory = remember { mutableStateListOf<CardInstance>() }

    var showResultDialog by remember { mutableStateOf(false) }
    var lastDrawResults by remember { mutableStateOf<List<CardInstance>>(emptyList()) }
    var showRechargePrompt by remember { mutableStateOf(false) }

    // 特效状态
    var isAnimating by remember { mutableStateOf(false) }
    var drawTier by remember { mutableStateOf(DrawTier.POOR) }

    val resetBox = {
        epicPool = (1..settingEpic).toList()
        showTimePool = (1..settingShowTime).toList()
        bigTimePool = (1..settingBigTime).toList()
        featuredPool = (1..settingFeatured).toList()
        remainNormal = settingNormal
        inventory.clear()
        Toast.makeText(context, "卡池已按比例重置", Toast.LENGTH_SHORT).show()
    }

    val drawCards = { count: Int, cost: Int ->
        if (coins < cost) {
            showRechargePrompt = true
        } else if (remainTotal < count) {
            Toast.makeText(context, "卡池剩余球员不足 $count 名", Toast.LENGTH_SHORT).show()
        } else {
            coins -= cost
            val results = mutableListOf<CardInstance>()

            for (i in 0 until count) {
                val currentTotal = epicPool.size + showTimePool.size + bigTimePool.size + featuredPool.size + remainNormal
                val rand = Random.nextInt(currentTotal)

                // ★ 这里就是保证“每个人概率绝对相等”的核心逻辑！去掉了容易报错的also语法
                val drawnCard = when {
                    rand < epicPool.size -> {
                        val num = epicPool.random()
                        epicPool = epicPool - num
                        CardInstance(CardRarity.EPIC, num)
                    }
                    rand < epicPool.size + showTimePool.size -> {
                        val num = showTimePool.random()
                        showTimePool = showTimePool - num
                        CardInstance(CardRarity.SHOWTIME, num)
                    }
                    rand < epicPool.size + showTimePool.size + bigTimePool.size -> {
                        val num = bigTimePool.random()
                        bigTimePool = bigTimePool - num
                        CardInstance(CardRarity.BIGTIME, num)
                    }
                    rand < epicPool.size + showTimePool.size + bigTimePool.size + featuredPool.size -> {
                        val num = featuredPool.random()
                        featuredPool = featuredPool - num
                        CardInstance(CardRarity.FEATURED, num)
                    }
                    else -> {
                        remainNormal--
                        CardInstance(CardRarity.NORMAL, null)
                    }
                }

                results.add(drawnCard)
                if (drawnCard.rarity != CardRarity.NORMAL) {
                    inventory.add(drawnCard)
                }
            }
            lastDrawResults = results.sortedBy { it.rarity.ordinal }

            // 判断出货档次
            val hasGood = results.any { it.rarity == CardRarity.EPIC || it.rarity == CardRarity.SHOWTIME || it.rarity == CardRarity.BIGTIME }
            val hasFeatured = results.any { it.rarity == CardRarity.FEATURED }

            drawTier = when {
                hasGood -> DrawTier.EXCELLENT
                hasFeatured -> DrawTier.GOOD
                else -> DrawTier.POOR
            }

            isAnimating = true

            // 动画总时长 3 秒
            coroutineScope.launch {
                delay(3000)
                isAnimating = false
                showResultDialog = true
            }
        }
    }

    // --- UI 布局 ---
    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                enumValues<EfootballTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White, selectedTextColor = Color(0xFF0F172A),
                            indicatorColor = Color(0xFF0F172A), unselectedIconColor = Color(0xFF94A3B8), unselectedTextColor = Color(0xFF94A3B8)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC)).padding(paddingValues)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFF7DD3FC).copy(alpha = 0.4f), Color.Transparent), center = Offset(size.width * 0.8f, size.height * 0.1f), radius = size.width * 0.7f))
                drawCircle(brush = Brush.radialGradient(colors = listOf(Color(0xFFD8B4FE).copy(alpha = 0.4f), Color.Transparent), center = Offset(size.width * 0.2f, size.height * 0.8f), radius = size.width * 0.7f))
            }

            Column(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.background(Color.White, RoundedCornerShape(12.dp))) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Color(0xFF0F172A))
                    }
                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 2.dp) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            GoldCoinIcon(size = 20)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$coins", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                    when (currentTab) {
                        EfootballTab.Gacha -> GachaScreen(
                            remainTotal, epicPool.size, showTimePool.size, bigTimePool.size, featuredPool.size, remainNormal,
                            onSingleDraw = { drawCards(1, 100) }, onTenDraw = { drawCards(10, 900) }
                        )
                        EfootballTab.Inventory -> InventoryScreen(inventory, resetBox)
                        EfootballTab.Recharge -> RechargeScreen { amount -> coins += amount; Toast.makeText(context, "成功充值 $amount 金币！", Toast.LENGTH_SHORT).show() }
                        EfootballTab.Settings -> SettingsScreen(
                            settingEpic, settingShowTime, settingBigTime, goodCardsCount, settingFeatured, settingNormal, settingTotal,
                            onSettingsChange = { e, s, b -> settingEpic = e; settingShowTime = s; settingBigTime = b }, onReset = resetBox
                        )
                    }
                }
            }
        }

        // ================= 抽卡特效全屏遮罩 (带升格逻辑) =================
        if (isAnimating) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .pointerInput(Unit) { detectTapGestures { } },
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()

                // 呼吸和旋转动画
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.8f, targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(animation = tween(800, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
                )
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f, targetValue = 360f,
                    animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
                )

                // 进度动画，用于控制升格时机 (0f -> 1f，耗时3秒)
                val animProgress = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    animProgress.animateTo(1f, animationSpec = tween(3000, easing = LinearEasing))
                }

                // 颜色逻辑
                val baseColor = if (drawTier == DrawTier.POOR) Color(0xFF94A3B8) else Color(0xFF4ADE80) // 灰色 或 绿色

                // 计算彩虹色的透明度 (只在 EXCELLENT 档次，且进度 > 0.4 也就是 1.2秒 后开始渐变出现)
                val rainbowAlpha = if (drawTier == DrawTier.EXCELLENT) {
                    ((animProgress.value - 0.4f) * 10f).coerceIn(0f, 1f)
                } else 0f

                val rainbowColors = listOf(Color.Red, Color(0xFFFF9800), Color.Yellow, Color.Green, Color.Blue, Color(0xFF9C27B0), Color.Red)

                // 旋转的光阵
                Box(modifier = Modifier.size(220.dp).scale(scale).rotate(rotation)) {
                    // 底层光阵 (灰色或绿色)
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.sweepGradient(listOf(baseColor.copy(alpha = 0f), baseColor, baseColor.copy(alpha = 0f))),
                        shape = CircleShape
                    ))

                    // 升格彩虹光阵 (覆盖在上面，根据透明度显现)
                    if (rainbowAlpha > 0f) {
                        Box(modifier = Modifier.fillMaxSize().alpha(rainbowAlpha).background(
                            Brush.sweepGradient(rainbowColors),
                            shape = CircleShape
                        ))
                    }
                }

                // 中心发光核心
                val centerBorderBrush = if (rainbowAlpha > 0f) Brush.sweepGradient(rainbowColors) else SolidColor(baseColor)

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.White, CircleShape)
                        .border(4.dp, centerBorderBrush, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GoldCoinIcon(size = 40)
                }

                // 动态提示文字
                val loadingText = if (drawTier == DrawTier.EXCELLENT && animProgress.value > 0.45f) {
                    "✨ 发现顶级球员！✨"
                } else {
                    "正在连线经纪人..."
                }

                Text(
                    text = loadingText,
                    color = if (rainbowAlpha > 0.5f) Color(0xFFFFD700) else Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = 300.dp)
                )
            }
        }

        // 抽卡结果弹窗
        if (showResultDialog) {
            AlertDialog(
                onDismissRequest = { showResultDialog = false },
                title = { Text("签约结果", fontWeight = FontWeight.Bold) },
                text = {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(lastDrawResults) { card -> CardItemView(card) }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showResultDialog = false }) { Text("确定", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                },
                containerColor = Color.White
            )
        }

        if (showRechargePrompt) {
            AlertDialog(
                onDismissRequest = { showRechargePrompt = false },
                title = { Text("金币不足", fontWeight = FontWeight.Bold, color = Color(0xFF0F172A)) },
                text = { Text("您的金币余额不足，是否前往充值获取更多金币？", fontSize = 16.sp) },
                confirmButton = {
                    Button(onClick = { showRechargePrompt = false; currentTab = EfootballTab.Recharge }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))) {
                        Text("去充值", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { showRechargePrompt = false }) { Text("取消", color = Color(0xFF64748B)) } },
                containerColor = Color.White
            )
        }
    }
}

// ================= 4. 独立卡片组件 =================

@Composable
fun CardItemView(card: CardInstance) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(card.rarity.color, RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(card.rarity.label, color = card.rarity.textColor, fontSize = 14.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        }

        if (card.number != null) {
            Box(modifier = Modifier.fillMaxSize().padding(bottom = 6.dp), contentAlignment = Alignment.BottomCenter) {
                Text(
                    text = "No.${card.number}",
                    color = card.rarity.textColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ================= 5. 四个子页面组件 =================

@Composable
fun GachaScreen(
    remainTotal: Int, remainEpic: Int, remainShowTime: Int, remainBigTime: Int, remainFeatured: Int, remainNormal: Int,
    onSingleDraw: () -> Unit, onTenDraw: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp), color = Color.White, shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("特殊经纪人 (Box Draw)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(8.dp))
                Text("$remainTotal", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                Text("剩余球员总数", fontSize = 14.sp, color = Color(0xFF94A3B8))

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PoolStatItem(CardRarity.EPIC, remainEpic)
                    PoolStatItem(CardRarity.SHOWTIME, remainShowTime)
                    PoolStatItem(CardRarity.BIGTIME, remainBigTime)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    PoolStatItem(CardRarity.FEATURED, remainFeatured)
                    PoolStatItem(CardRarity.NORMAL, remainNormal)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(
                onClick = onSingleDraw, modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF0F172A)), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("签下 1 名", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoldCoinIcon(size = 14)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("100", fontSize = 14.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Button(
                onClick = onTenDraw, modifier = Modifier.weight(1f).height(72.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A), contentColor = Color.White), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("签下 10 名", fontWeight = FontWeight.Black, fontSize = 20.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GoldCoinIcon(size = 14)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("900", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PoolStatItem(rarity: CardRarity, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.background(rarity.color, RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 4.dp)) {
            Text(rarity.label, color = rarity.textColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text("$count", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

@Composable
fun InventoryScreen(inventory: List<CardInstance>, onClear: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("稀有球员背包 (${inventory.size})", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
            TextButton(onClick = onClear) { Text("清空并重置卡池", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold) }
        }

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("背包空空如也，普卡已被自动清理！", color = Color(0xFF94A3B8), fontSize = 16.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)
            ) {
                items(inventory.reversed()) { card -> CardItemView(card) }
            }
        }
    }
}

@Composable
fun RechargeScreen(onRecharge: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("充值中心", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), modifier = Modifier.padding(vertical = 16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(rechargeList) { option ->
                RechargeCard(option = option) { purchasedCoins ->
                    onRecharge(purchasedCoins)
                }
            }
        }
    }
}

@Composable
fun RechargeCard(option: RechargeOption, onClick: (Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick(option.coins + option.bonus) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoldCoinIcon(size = 24)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${option.coins}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF2C3040)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF4661F6), RoundedCornerShape(8.dp))
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    Text("¥ ${option.price}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            if (option.bonus > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            Color(0xFFEF4444),
                            RoundedCornerShape(bottomStart = 12.dp, topEnd = 16.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("+${option.bonus}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    epic: Int, showTime: Int, bigTime: Int, goodCount: Int, featured: Int, normal: Int, total: Int,
    onSettingsChange: (Int, Int, Int) -> Unit, onReset: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("卡池设置 (自动配比)", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A), modifier = Modifier.padding(vertical = 16.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingRow("Epic 数量", epic) { onSettingsChange(it, showTime, bigTime) }
                SettingRow("ShowTime 数量", showTime) { onSettingsChange(epic, it, bigTime) }
                SettingRow("BigTime 数量", bigTime) { onSettingsChange(epic, showTime, it) }

                Divider(color = Color(0xFFE2E8F0), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))

                Text("卡池生成规则: 1张好卡 = 4张精选 + 45张普卡", fontSize = 12.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("当前好卡总数:", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("$goodCount", color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("自动配置精选:", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("$featured", color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("自动配置普卡:", color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                    Text("$normal", color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("卡池总容量:", color = Color(0xFFF5576C), fontWeight = FontWeight.Bold)
                    Text("$total", color = Color(0xFFF5576C), fontWeight = FontWeight.Black)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onReset, modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                ) {
                    Text("保存并重置卡池", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SettingRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontWeight = FontWeight.Bold, color = Color(0xFF334155), fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { if (value > 0) onValueChange(value - 1) }, modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape).size(36.dp)) {
                Text("-", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
            Text("$value", modifier = Modifier.width(48.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            IconButton(onClick = { onValueChange(value + 1) }, modifier = Modifier.background(Color(0xFFF1F5F9), CircleShape).size(36.dp)) {
                Text("+", fontWeight = FontWeight.Black, fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EfootballMainScreenPreview() {
    EfootballMainScreen()
}
