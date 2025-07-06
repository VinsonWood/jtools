package com.jtools.jellyfin.gui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jtools.jellyfin.model.JellyfinMovie
import com.jtools.jellyfin.model.JellyfinPerson

@Composable
fun MovieCardsGrid(movies: List<JellyfinMovie>, modifier: Modifier = Modifier) {
    if (movies.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.Movie,
            title = "没有电影",
            subtitle = "此文件中没有收藏的电影",
            modifier = modifier
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 280.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(movies) { movie ->
            MovieCard(movie = movie)
        }
    }
}

@Composable
fun ActorCardsGrid(people: List<JellyfinPerson>, modifier: Modifier = Modifier) {
    if (people.isEmpty()) {
        EmptyStateCard(
            icon = Icons.Default.Person,
            title = "没有演员",
            subtitle = "此文件中没有收藏的演员",
            modifier = modifier
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 250.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(people) { person ->
            ActorCard(person = person)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieCard(movie: JellyfinMovie) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 标题和年份
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                movie.productionYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 类型信息
            movie.genres?.let { genres ->
                if (genres.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(genres.take(3)) { genre ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = genre,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // 底部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 电影图标
                Icon(
                    Icons.Default.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                
                // ID信息
                Text(
                    text = "ID: ${movie.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActorCard(person: JellyfinPerson) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 演员名字
            Text(
                text = person.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // 职业类型
            person.type?.let { type ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = when (type) {
                            "Person" -> "演员"
                            "Director" -> "导演"
                            "Writer" -> "编剧"
                            else -> type
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // 底部信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 演员图标
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                
                // ID信息
                Text(
                    text = "ID: ${person.id.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}