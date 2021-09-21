package CloudSimWrappers

import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerBestFit, DatacenterBrokerFirstFit, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.{CloudSim, Simulation}

class ConfiguredBroker(simulation: CloudSim, config: Config) {

  def getConfiguredBroker(): DatacenterBroker = {
    val broker:DatacenterBroker = config.getString("broker.BrokerType") match {
      case "Simple" => new DatacenterBrokerSimple(simulation)
      case "BestFit" => new DatacenterBrokerBestFit(simulation)
      case "FirstFit" => new DatacenterBrokerFirstFit(simulation)
      case _ => throw new RuntimeException("Invalid broker type specified")
    }
    //broker.setVmDestructionDelay(config.getInt("broker.VMDestructionDelay"))
    broker
  }
}
