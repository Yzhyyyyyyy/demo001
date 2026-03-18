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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- 全局色彩 ---
val PeDark = Color(0xFF0F172A)
val PeGray = Color(0xFF64748B)
val PeBorder = Color(0xFFE2E8F0)
val PeYellow = Color(0xFFFBBF24)

val ElementColors = listOf(
    Color(0xFF4FACFE), Color(0xFFA78BFA), Color(0xFFF472B6), Color(0xFF2DD4BF), Color(0xFFFBBF24)
)

// 底部导航 4 个 Tab (充值和兑换分离)
enum class PeTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Roulette("扭蛋", Icons.Filled.Refresh),
    Recharge("充值", Icons.Filled.AddCircle),
    Exchange("兑换", Icons.Filled.ShoppingCart),
    Inventory("仓库", Icons.AutoMirrored.Filled.List)
}

// 外圈 8 个孔的星级分布 (0代表失败空孔)
val OuterSlots = listOf(1, 0, 2, 1, 0, 3, 1, 0)
val ProtectCosts = listOf(0, 6, 17, 51, 153, 430, 827)
val StarRewards = listOf(0, 12, 36, 108, 320, 960, 2880)

@Composable
fun PeUnifiedBackground(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FAFC))) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(brush = Brush.radialGradient(listOf(Color(0xFF7DD3FC).copy(alpha = 0.65f), Color.Transparent), center = Offset(size.width * 0.8f, size.height * 0.1f), radius = size.width * 0.75f))
            drawCircle(brush = Brush.radialGradient(listOf(Color(0xFFD8B4FE).copy(alpha = 0.6f), Color.Transparent), center = Offset(size.width * 0.2f, size.height * 0.8f), radius = size.width * 0.75f))
        }
        content()
    }
}

@Composable
fun PeaceEliteScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- 全局资产状态 ---
    var luckyCoins by remember { mutableIntStateOf(128) }
    var fragments by remember { mutableIntStateOf(0) }
    var badges by remember { mutableIntStateOf(0) }
    val inventory = remember { mutableStateMapOf<String, Int>() }
    var currentTab by remember { mutableStateOf(PeTab.Roulette) }

    // --- 核心修复：将转盘状态提升到全局，切换 Tab 绝不重置 ---
    var currentStar by remember { mutableIntStateOf(0) }
    var currentType by remember { mutableIntStateOf(-1) }
    var protectFailCount by remember { mutableIntStateOf(0) }
    var isRoundActive by remember { mutableStateOf(false) }
    var isSpinning by remember { mutableStateOf(false) }

    val innerRotationAnim = remember { Animatable(0f) }
    val outerRotationAnim = remember { Animatable(0f) }

    fun getStarIncrease() = when {
        Random.nextFloat() < 0.82f -> 1
        Random.nextFloat() < 0.95f -> 2
        else -> 3
    }
    fun getStarDecrease() = if (Random.nextFloat() < 0.75f) 1 else 2

    fun claimReward(star: Int, isPity: Boolean = false) {
        if (star >= 7) {
            badges += 1
            Toast.makeText(context, "获得 兑换徽章*1！", Toast.LENGTH_LONG).show()
        } else if (star > 0) {
            val frags = StarRewards[star]
            fragments += frags
            Toast.makeText(context, "领取奖励：扭蛋碎片*$frags", Toast.LENGTH_SHORT).show()
        } else if (isPity) {
            Toast.makeText(context, "追加失败！触发保底奖励", Toast.LENGTH_SHORT).show()
        }
        isRoundActive = false
        currentStar = 0
        currentType = -1
        protectFailCount = 0
    }

    fun executeSpin(isStart: Boolean, isProtect: Boolean = false) {
        coroutineScope.launch {
            isSpinning = true

            val targetColorType = if (isProtect && protectFailCount >= 2) currentType else Random.nextInt(5)
            val isSuccess = isStart || (targetColorType == currentType)
            val targetStarVal = if (isSuccess) getStarIncrease() else 0

            val validSlots = OuterSlots.mapIndexedNotNull { index, stars -> if (stars == targetStarVal) index else null }
            val targetOuterSlot = validSlots.random()

            val innerTargetMod = 360f - (targetColorType * 72f)
            val currentInnerRem = innerRotationAnim.value % 360f
            val innerBase = innerRotationAnim.value - currentInnerRem + if (currentInnerRem > 0) 360f else 0f
            val finalInner = innerBase + (5 * 360f) + innerTargetMod

            val currentOuterRem = outerRotationAnim.value % 360f
            val outerBase = outerRotationAnim.value - currentOuterRem - if (currentOuterRem < 0) 360f else 0f
            val finalOuter = outerBase - (4 * 360f) - (targetOuterSlot * 45f)

            launch { innerRotationAnim.animateTo(finalInner, tween(3500, easing = FastOutSlowInEasing)) }
            launch { outerRotationAnim.animateTo(finalOuter, tween(3500, easing = FastOutSlowInEasing)) }
            delay(3500)

            if (isStart) {
                currentType = targetColorType
                currentStar = targetStarVal
                isRoundActive = true
                protectFailCount = 0
            } else {
                if (isSuccess) {
                    currentStar = minOf(7, currentStar + targetStarVal)
                    protectFailCount = 0
                    Toast.makeText(context, "追加成功！星级 +$targetStarVal", Toast.LENGTH_SHORT).show()
                } else {
                    if (isProtect) {
                        protectFailCount++
                        Toast.makeText(context, "追加失败，已保护星级", Toast.LENGTH_SHORT).show()
                    } else {
                        val drop = getStarDecrease()
                        val oldStar = currentStar
                        currentStar -= drop
                        if (currentStar > 0) {
                            Toast.makeText(context, "追加失败，降至 $currentStar 星", Toast.LENGTH_SHORT).show()
                            claimReward(currentStar)
                        } else {
                            val pityFrags = if (oldStar == 1) 2 else 4
                            fragments += pityFrags
                            claimReward(0, isPity = true)
                        }
                    }
                }
            }
            isSpinning = false
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White, tonalElevation = 8.dp,
                modifier = Modifier.border(1.dp, PeBorder, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)).clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                enumValues<PeTab>().forEach { tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontWeight = FontWeight.Bold) },
                        selected = currentTab == tab, onClick = { currentTab = tab },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, selectedTextColor = PeDark, indicatorColor = PeDark, unselectedIconColor = PeGray)
                    )
                }
            }
        }
    ) { paddingValues ->
        PeUnifiedBackground {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 16.dp).align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(Color.White, CircleShape).border(1.dp, PeBorder, CircleShape).clickable { onBackClick() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = PeDark) }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).border(1.dp, PeBorder, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🧩", fontSize = 14.sp); Spacer(Modifier.width(6.dp)); Text("$fragments", fontSize = 14.sp, fontWeight = FontWeight.Black, color = PeDark)
                        }
                        Row(modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp)).border(1.dp, PeBorder, RoundedCornerShape(20.dp)).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 14.sp); Spacer(Modifier.width(6.dp)); Text("$luckyCoins", fontSize = 14.sp, fontWeight = FontWeight.Black, color = PeDark)
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp, start = 24.dp, end = 24.dp)) {
                    when (currentTab) {
                        PeTab.Roulette -> PeRouletteTab(
                            coins = luckyCoins, currentStar = currentStar, currentType = currentType,
                            protectFailCount = protectFailCount, isRoundActive = isRoundActive, isSpinning = isSpinning,
                            innerRotation = innerRotationAnim.value, outerRotation = outerRotationAnim.value,
                            onSpinClick = { isStart, isProtect ->
                                if (luckyCoins < (if(isStart) 6 else ProtectCosts[currentStar])) Toast.makeText(context, "幸运币不足", Toast.LENGTH_SHORT).show()
                                else { luckyCoins -= if(isStart) 6 else ProtectCosts[currentStar]; executeSpin(isStart, isProtect) }
                            },
                            onClaimClick = { claimReward(it) }
                        )
                        PeTab.Recharge -> PeRechargeTab { amount -> luckyCoins += amount }
                        PeTab.Exchange -> PeExchangeTab(fragments, badges, { fragments = it }, { badges = it }, { inventory[it] = (inventory[it] ?: 0) + 1 })
                        PeTab.Inventory -> PeInventoryTab(inventory, badges)
                    }
                }
            }
        }
    }
}

// ==================== 1. 扭蛋大转盘 ====================
@Composable
fun PeRouletteTab(
    coins: Int, currentStar: Int, currentType: Int, protectFailCount: Int,
    isRoundActive: Boolean, isSpinning: Boolean, innerRotation: Float, outerRotation: Float,
    onSpinClick: (Boolean, Boolean) -> Unit, onClaimClick: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, PeBorder, RoundedCornerShape(16.dp)).padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                for (i in 1..7) {
                    val isActive = currentStar >= i
                    val isCurrent = currentStar == i
                    Box(
                        modifier = Modifier.size(if (isCurrent) 36.dp else 28.dp).clip(CircleShape)
                            .background(if (isActive) Brush.linearGradient(listOf(Color(0xFF60A5FA), Color(0xFF3B82F6))) else SolidColor(Color(0xFFF1F5F9)))
                            .border(if (isCurrent) 2.dp else 1.dp, if (isCurrent) Color(0xFF93C5FD) else PeBorder, CircleShape)
                            .shadow(if (isActive) 4.dp else 0.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("$i", color = if (isActive) Color.White else PeGray, fontWeight = FontWeight.Black, fontSize = 12.sp) }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(340.dp)) { drawCircle(brush = Brush.radialGradient(colors = listOf(Color.White.copy(alpha = 0.8f), Color.Transparent))) }

            // 1. 外圈 (已修改为轻奢铂金银白色调)
            Box(modifier = Modifier.size(300.dp).rotate(outerRotation)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // 铂金质感渐变底色
                    drawCircle(brush = Brush.sweepGradient(listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0), Color(0xFFF8FAFC), Color(0xFFE2E8F0), Color(0xFFF1F5F9))), radius = size.width / 2)
                    drawCircle(color = PeBorder, radius = size.width / 2, style = Stroke(width = 2.dp.toPx()))
                    for (i in 0..7) {
                        val angle = (i * 45f - 90f + 22.5f) * (Math.PI / 180f).toFloat()
                        drawLine(color = PeBorder, start = Offset(size.width / 2, size.height / 2),
                            end = Offset(size.width / 2 + (size.width / 2) * kotlin.math.cos(angle.toDouble()).toFloat(), size.height / 2 + (size.height / 2) * kotlin.math.sin(angle.toDouble()).toFloat()), strokeWidth = 2.dp.toPx()
                        )
                    }
                }
                for (i in 0..7) {
                    Box(modifier = Modifier.fillMaxSize().rotate(i * 45f)) {
                        val stars = OuterSlots[i]
                        Box(
                            // 星级孔位改为纯白背景 + 微阴影
                            modifier = Modifier.align(Alignment.TopCenter).offset(y = 12.dp).size(42.dp)
                                .shadow(2.dp, CircleShape)
                                .background(Color.White, CircleShape)
                                .border(1.dp, PeBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (stars == 0) {
                                // 失败的空孔，用浅灰色小圆点表示
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFCBD5E1), CircleShape))
                            } else {
                                Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    repeat(stars) { Icon(Icons.Filled.Star, contentDescription = null, tint = PeYellow, modifier = Modifier.size(12.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            // 2. 内圈 (冰透白)
            Box(modifier = Modifier.size(180.dp).rotate(innerRotation)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(brush = Brush.linearGradient(colors = listOf(Color(0xFFF8FAFC), Color(0xFFE0F2FE))), radius = size.width / 2)
                    drawCircle(brush = Brush.sweepGradient(colors = listOf(Color(0xFF7DD3FC), Color(0xFFD8B4FE), Color(0xFFF472B6), Color(0xFF7DD3FC))), radius = size.width / 2, style = Stroke(width = 4.dp.toPx()))
                    for (i in 0..4) {
                        val angle = (i * 72f - 90f + 36f) * (Math.PI / 180f).toFloat()
                        drawLine(color = PeBorder, start = Offset(size.width / 2, size.height / 2),
                            end = Offset(size.width / 2 + (size.width / 2) * kotlin.math.cos(angle.toDouble()).toFloat(), size.height / 2 + (size.height / 2) * kotlin.math.sin(angle.toDouble()).toFloat()), strokeWidth = 2.dp.toPx()
                        )
                    }
                    drawCircle(color = Color.White, radius = 28.dp.toPx())
                    drawCircle(color = PeBorder, radius = 28.dp.toPx(), style = Stroke(width = 2.dp.toPx()))
                }
                for (i in 0..4) {
                    Box(modifier = Modifier.fillMaxSize().rotate(i * 72f)) {
                        Box(
                            modifier = Modifier.align(Alignment.TopCenter).offset(y = 12.dp).size(40.dp).shadow(4.dp, CircleShape).clip(CircleShape)
                                .background(Brush.radialGradient(colors = listOf(ElementColors[i].copy(alpha = 0.6f), ElementColors[i]), center = Offset(20f, 20f), radius = 80f))
                                .border(1.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(modifier = Modifier.align(Alignment.TopStart).offset(x = 6.dp, y = 6.dp).size(8.dp).background(Color.White.copy(alpha = 0.6f), CircleShape))
                            if (isRoundActive && currentType == i && !isSpinning) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-24).dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(32.dp).background(PeDark, CircleShape).border(2.dp, Color.White, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "指针", tint = Color.White)
                }
                Box(modifier = Modifier.width(4.dp).height(56.dp).background(PeDark, RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp)))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isRoundActive && !isSpinning) {
                    Text("当前星级", color = PeGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("$currentStar", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PeDark)
                } else if (isSpinning) {
                    Text("抽取中", color = PeGray, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                } else {
                    Text("等待", color = PeGray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Text("启动", fontSize = 18.sp, fontWeight = FontWeight.Black, color = PeDark)
                }
            }
        }

        if (isRoundActive && currentStar < 7) {
            Text("当前可领: 碎片 * ${StarRewards[currentStar]}", fontSize = 14.sp, color = PeDark, fontWeight = FontWeight.Bold)
            if (protectFailCount > 0) {
                Text("保护追加已垫: $protectFailCount 次 (第3次必成)", color = Color(0xFFEF4444), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }
        } else if (currentStar >= 7) {
            Text("已达到最高星级！请领取徽章", color = Color(0xFFEF4444), fontWeight = FontWeight.Black)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (!isRoundActive) {
            Button(
                onClick = { onSpinClick(true, false) }, enabled = !isSpinning, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PeDark, contentColor = Color.White), elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) { Text(text = if (isSpinning) "转动中..." else "启动转盘 (6 幸运币)", fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp) }
        } else {
            if (currentStar < 7) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { onClaimClick(currentStar) }, enabled = !isSpinning, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PeDark), border = BorderStroke(1.dp, PeBorder)
                    ) { Text("领取奖励", fontWeight = FontWeight.Bold) }

                    Button(
                        onClick = { onSpinClick(false, false) }, enabled = !isSpinning, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = PeGray), border = BorderStroke(1.dp, PeBorder)
                    ) { Text("普通追加\n(免费)", textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Bold) }

                    val pCost = ProtectCosts[currentStar]
                    Button(
                        onClick = { onSpinClick(false, true) }, enabled = !isSpinning, modifier = Modifier.weight(1f).height(60.dp), shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PeDark, contentColor = PeYellow), elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) { Text("保护追加\n($pCost 币)", textAlign = TextAlign.Center, fontSize = 12.sp, fontWeight = FontWeight.Black) }
                }
            } else {
                Button(
                    onClick = { onClaimClick(currentStar) }, modifier = Modifier.fillMaxWidth().height(64.dp), shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PeDark, contentColor = PeYellow)
                ) { Text("领取 兑换徽章", fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp) }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ==================== 2. 充值中心 ====================
@Composable
fun PeRechargeTab(onRecharge: (Int) -> Unit) {
    val context = LocalContext.current
    val rechargeOptions = listOf(6 to 60, 30 to 300, 68 to 680, 128 to 1280, 198 to 1980, 328 to 3280, 648 to 6480)
    val chunkedOptions = rechargeOptions.chunked(3)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("充值中心", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PeDark, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("RECHARGE", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PeGray, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            chunkedOptions.forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    rowItems.forEach { (price, amount) ->
                        Button(
                            onClick = { onRecharge(amount); Toast.makeText(context, "充值成功：$amount 币", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = if (price == 648) PeDark else Color.White, contentColor = if (price == 648) PeYellow else PeDark),
                            border = if (price == 648) null else BorderStroke(1.dp, PeBorder)
                        ) { Text("¥$price\n$amount 币", textAlign = TextAlign.Center, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
                    }
                    if (rowItems.size < 3) { repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) } }
                }
            }
        }
    }
}

// ==================== 3. 兑换商店 ====================
@Composable
fun PeExchangeTab(fragments: Int, badges: Int, onFragmentsChange: (Int) -> Unit, onBadgesChange: (Int) -> Unit, onAddItem: (String) -> Unit) {
    val context = LocalContext.current
    var showExchangeDialog by remember { mutableStateOf<String?>(null) }

    if (showExchangeDialog != null) {
        AlertDialog(
            onDismissRequest = { showExchangeDialog = null },
            title = { Text("🎉 兑换成功！", fontWeight = FontWeight.Black, color = PeDark) },
            text = { Text("恭喜您获得了【${showExchangeDialog}】\n物品已发放至您的仓库，快去看看吧！", color = PeGray, lineHeight = 20.sp) },
            confirmButton = {
                Button(onClick = { showExchangeDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = PeDark, contentColor = Color.White), shape = RoundedCornerShape(8.dp)) { Text("确定", fontWeight = FontWeight.Bold) }
            },
            containerColor = Color.White, shape = RoundedCornerShape(16.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("兑换商店", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PeDark, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("EXCHANGE SHOP", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PeGray, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, PeBorder, RoundedCornerShape(16.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text("极品载具皮肤", fontWeight = FontWeight.Black, color = PeDark, fontSize = 16.sp)
            Button(
                onClick = {
                    if (badges >= 1) { onBadgesChange(badges - 1); onAddItem("极品载具皮肤"); showExchangeDialog = "极品载具皮肤" }
                    else if (fragments >= 2880) { onFragmentsChange(fragments - 2880); onAddItem("极品载具皮肤"); showExchangeDialog = "极品载具皮肤" }
                    else Toast.makeText(context, "徽章或碎片不足", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PeDark, contentColor = PeYellow), shape = RoundedCornerShape(12.dp)
            ) { Text("1徽章 / 2880碎片", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp).background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, PeBorder, RoundedCornerShape(16.dp)).padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Text("粉色极品套装", fontWeight = FontWeight.Black, color = PeDark, fontSize = 16.sp)
            Button(
                onClick = {
                    if (fragments >= 960) { onFragmentsChange(fragments - 960); onAddItem("粉色极品套装"); showExchangeDialog = "粉色极品套装" }
                    else Toast.makeText(context, "碎片不足", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = PeDark), shape = RoundedCornerShape(12.dp)
            ) { Text("960 碎片", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

// ==================== 4. 仓库 ====================
@Composable
fun PeInventoryTab(inventory: Map<String, Int>, badges: Int) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("我的仓库", fontSize = 32.sp, fontWeight = FontWeight.Black, color = PeDark, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("INVENTORY", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = PeGray, letterSpacing = 6.sp)
        Spacer(modifier = Modifier.height(24.dp))

        if (badges > 0) {
            Row(
                modifier = Modifier.padding(bottom = 16.dp).background(PeDark, RoundedCornerShape(12.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = PeYellow, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("兑换徽章: $badges", color = PeYellow, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
        }
        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("仓库空空如也", color = PeGray, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(inventory.toList()) { (name, count) ->
                    Column(
                        modifier = Modifier.background(Color.White, RoundedCornerShape(16.dp)).border(1.dp, PeBorder, RoundedCornerShape(16.dp)).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(name, fontWeight = FontWeight.Black, color = PeDark, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("数量: $count", color = PeGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PeaceEliteScreenPreview() {
    PeaceEliteScreen(onBackClick = {})
}