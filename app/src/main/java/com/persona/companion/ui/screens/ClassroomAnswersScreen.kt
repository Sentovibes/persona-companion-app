package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.models.ClassroomQuestion
import com.persona.companion.models.QuestionType
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.ClassroomAnswerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomAnswersScreen(
    gameId: String,
    gameName: String,
    onBack: () -> Unit
) {
    val viewModel: ClassroomAnswerViewModel = viewModel()
    val classroomData by viewModel.classroomData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    LaunchedEffect(gameId) {
        viewModel.loadClassroomAnswers(gameId)
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Classroom Answers - $gameName",
                        color = TextPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentBlue
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                classroomData != null -> {
                    ClassroomQuestionsList(
                        questions = classroomData!!.questions
                    )
                }
            }
        }
    }
}

@Composable
private fun ClassroomQuestionsList(
    questions: List<ClassroomQuestion>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(questions) { question ->
            ClassroomQuestionCard(question = question)
        }
    }
}

@Composable
private fun ClassroomQuestionCard(
    question: ClassroomQuestion
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and type header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = question.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Exclusive badge
                    if (question.isExclusive) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Persona5Red.copy(alpha = 0.2f),
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "EXCLUSIVE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Persona5Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Type badge
                    if (question.type != QuestionType.CLASS) {
                        Box(
                            modifier = Modifier
                                .background(
                                    when (question.type) {
                                        QuestionType.EXAM -> AccentGreen.copy(alpha = 0.2f)
                                        QuestionType.MIDTERM -> AccentBlue.copy(alpha = 0.2f)
                                        QuestionType.FINAL -> AccentRed.copy(alpha = 0.2f)
                                        else -> AccentBlue.copy(alpha = 0.2f)
                                    },
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = question.type.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = when (question.type) {
                                    QuestionType.EXAM -> AccentGreen
                                    QuestionType.MIDTERM -> AccentBlue
                                    QuestionType.FINAL -> AccentRed
                                    else -> AccentBlue
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Question
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Answer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AccentBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "Answer:",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = question.answer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Subject (if exam)
            if (question.subject != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Subject: ${question.subject}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
