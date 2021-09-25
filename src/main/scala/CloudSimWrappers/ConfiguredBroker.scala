package CloudSimWrappers

import com.typesafe.config.Config
import org.cloudbus.cloudsim.brokers.{DatacenterBroker, DatacenterBrokerBestFit, DatacenterBrokerFirstFit, DatacenterBrokerSimple}
import org.cloudbus.cloudsim.core.{CloudSim, Simulation}
import org.cloudbus.cloudsim.datacenters.network.NetworkDatacenter

/**
 * Wrapper class to create a @Link{DatacenterBroker} based on the config file passed
 * @param simulation - the cloudsim simulatinon instance
 * @param config
 */
class ConfiguredBroker(simulation: CloudSim, config: Config) {

  /**
   * Get the configured datacenter broker.
   * Mathces the config string passed to the appropriate broker
   * @return DatacenterBroker
   */
  def getConfiguredBroker(): DatacenterBroker = {
    val broker:DatacenterBroker = config.getString("broker.BrokerType") match {
      case "Simple" => new DatacenterBrokerSimple(simulation)
      case "BestFit" => new DatacenterBrokerBestFit(simulation)
      case "FirstFit" => new DatacenterBrokerFirstFit(simulation)
      case _ => throw new RuntimeException("Invalid broker type specified")
    }

    broker
  }
}
