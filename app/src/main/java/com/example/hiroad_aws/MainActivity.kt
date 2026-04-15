package com.example.hiroad_aws

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.hiroad_aws.data.QuizModules
import com.example.hiroad_aws.ui.HomeScreen
import com.example.hiroad_aws.ui.QuizScreen
import com.example.hiroad_aws.ui.theme.HiRoad_AWSTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HiRoad_AWSTheme {
                var showQuiz by remember { mutableStateOf(false) }
                var moduleFilter by remember { mutableStateOf<String?>(null) }
                if (!showQuiz) {
                    HomeScreen(
                        modifier = Modifier.fillMaxSize(),
                        onModuleSelected = { title ->
                            moduleFilter =
                                if (title == QuizModules.ALL_QUESTIONS) null else title
                            showQuiz = true
                        },
                    )
                } else {
                    QuizScreen(
                        modifier = Modifier.fillMaxSize(),
                        moduleFilter = moduleFilter,
                        onExitToHome = { showQuiz = false },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizAppPreview() {
    HiRoad_AWSTheme {
        QuizScreen()
    }
}