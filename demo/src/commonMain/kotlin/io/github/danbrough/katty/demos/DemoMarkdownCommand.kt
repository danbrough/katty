package io.github.danbrough.katty.demos

import com.github.ajalt.mordant.markdown.Markdown
import io.github.danbrough.katty.BasicCommand


val DemoMarkDownCommand = BasicCommand("demo markdown") {
  val content = Markdown(
    """
    # A Title
    ## A subtitle
    ### A sub subtitle
    
    A "toml" block.
    
    ```toml
    [toml]
    message = 123
    
    [more_toml]
    state = { temp = 0.2, enabled = true }
    
    ```
    
    A kotlin block
    
    ```kotlin
    val s:String = "This is a message"
    fun test(){
      println("Hello World")
    }
    ```
    
    Some json.
        
    ```json
    {
      "firstName": "John",
      "lastName": "Smith",
      "age": 25
    }
    ```
  """.trimIndent()
  )
  terminal.println(content)
}
