package HelperUtils

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.{Failure, Success, Try}

class ObtainConfigReference(fileName:String){
  def getFileName = fileName
}
object ObtainConfigReference:
  private val logger = CreateLogger(classOf[ObtainConfigReference])
  
  private def ValidateConfig(config:Config,confEntry: String):Boolean = Try(config.getConfig(confEntry)) match {
    case Failure(exception) => logger.error(s"Failed to retrieve config entry $confEntry for reason $exception"); false
    case Success(_) => true
  }

  def getConfig(file:String,configPath:String): Option[Config] = {  
    val config:Config = Try(ConfigFactory.load(file)) match {
      case Failure(exception) => logger.error(s"Failed to retrieve config file $file for reason $exception"); ConfigFactory.load()
      case Success(_) => ConfigFactory.load(file)
    }

    if ValidateConfig(config,configPath) then Some(config.getConfig(configPath)) else None
    
  }

  def apply(fileName:String,confPath:String): Option[Config] = getConfig(fileName,confPath)
