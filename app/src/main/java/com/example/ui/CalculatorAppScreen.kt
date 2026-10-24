package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.tabs.AssistantTab
import com.example.ui.tabs.ConversionsTab
import com.example.ui.tabs.FormulasTab
import com.example.ui.tabs.PreciseCalculatorTab
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

data class NavItem(val label: String, val textIcon: String, val testTag: String)

@Composable
fun CalculatorAppScreen(viewModel: CalculatorViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedFormula by viewModel.selectedFormula.collectAsStateWithLifecycle()

    // Handle system back navigation cleanly
    BackHandler(enabled = selectedFormula != null || currentTab != "Calculator") {
        if (selectedFormula != null) {
            viewModel.selectFormula(null)
        } else if (currentTab != "Calculator") {
            viewModel.selectTab("Calculator")
        }
    }

    val baseGradient = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF030303),
                Color(0xFF000502),
                Color(0xFF000F05)
            )
        )
    }

    val navItems = remember {
        listOf(
            NavItem("Calculator", "7", "app_tab_calculator"),
            NavItem("Formulas", "f(x)", "app_tab_formulas"),
            NavItem("Conversions", "⇄", "app_tab_conversions"),
            NavItem("AI Chat", "AI", "app_tab_ai_chat")
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(baseGradient)
    ) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (!isWideScreen) {
                    BottomNavBar(
                        navItems = navItems,
                        selectedTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (isWideScreen) {
                    SidebarRail(
                        navItems = navItems,
                        selectedTab = currentTab,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .systemBarsPadding()
                ) {
                    when (currentTab) {
                        "Calculator" -> PreciseCalculatorTab(viewModel)
                        "Formulas" -> FormulasTab(viewModel)
                        "Conversions" -> ConversionsTab(viewModel)
                        "AI Chat" -> AssistantTab(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    navItems: List<NavItem>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    val navBarInsets = WindowInsets.navigationBars.asPaddingValues()

    NavigationBar(
        containerColor = Color(0xFF0A0A0A),
        tonalElevation = 8.dp,
        modifier = Modifier.height(68.dp + navBarInsets.calculateBottomPadding() / 2)
    ) {
        navItems.forEach { item ->
            val isSelected = selectedTab == item.label
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.label) },
                icon = {
                    Text(
                        text = item.textIcon,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (item.label == "Formulas") 14.sp else 18.sp,
                        color = if (isSelected) Color.Black else GreenPrimary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) GreenPrimary else TextMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GreenPrimary,
                    selectedIconColor = Color.Black,
                    unselectedIconColor = GreenPrimary
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

@Composable
fun SidebarRail(
    navItems: List<NavItem>,
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    NavigationRail(
        containerColor = Color(0xFF0A0A0A),
        modifier = Modifier.width(80.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "AI",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.ExtraBold,
            color = GreenPrimary,
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "CALC",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = TextWhite,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(40.dp))

        navItems.forEach { item ->
            val isSelected = selectedTab == item.label
            NavigationRailItem(
                selected = isSelected,
                onClick = { onTabSelected(item.label) },
                icon = {
                    Text(
                        text = item.textIcon,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (isSelected) Color.Black else GreenPrimary
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = GreenPrimary,
                    selectedIconColor = Color.Black,
                    selectedTextColor = GreenPrimary,
                    unselectedIconColor = GreenPrimary,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .testTag(item.testTag)
            )
        }
    }
}
