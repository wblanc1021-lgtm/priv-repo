package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RecastProject
import com.example.data.model.RecastVariant
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CharcoalCard
import com.example.ui.theme.CharcoalElevated
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.MutedIvory
import com.example.ui.theme.NeonOrange
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.WarmIvory

@Composable
fun VariantGridScreen(
    project: RecastProject,
    variants: List<RecastVariant>,
    onSelectVariant: (RecastVariant) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedVariantId by remember { mutableStateOf(variants.firstOrNull()?.id ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = WarmIvory)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "RECAST VARIANT GRID",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = WarmIvory
                )
                Text(
                    text = "Evaluate alternative character interpretations & lighting takes",
                    fontSize = 11.sp,
                    color = MutedIvory
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(variants) { variant ->
                val isSelected = variant.id == selectedVariantId
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF261408) else CharcoalCard)
                        .border(
                            1.5.dp,
                            if (isSelected) NeonOrange else BorderSubtle,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedVariantId = variant.id
                            onSelectVariant(variant)
                        }
                        .padding(10.dp)
                ) {
                    // Mock preview thumbnail card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(CharcoalElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "TAKE #${variant.variantIndex}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) NeonOrange else WarmIvory
                            )
                            Text(
                                text = "Fidelity ${(variant.scoreFidelity * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ChampagneGold
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(NeonOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = ObsidianBlack, modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = variant.previewLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarmIvory,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = variant.lightingStyle,
                        fontSize = 10.sp,
                        color = MutedIvory
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "S: ${variant.seed}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MutedIvory
                            )
                        }

                        Text(
                            text = if (isSelected) "ACTIVE" else "SELECT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) NeonOrange else WarmIvory
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                Toast.makeText(context, "Promoted Take to Master Render", Toast.LENGTH_SHORT).show()
                onBack()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonOrange,
                contentColor = ObsidianBlack
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("apply_variant_button")
        ) {
            Text("PROMOTE AS MASTER RECAST", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
