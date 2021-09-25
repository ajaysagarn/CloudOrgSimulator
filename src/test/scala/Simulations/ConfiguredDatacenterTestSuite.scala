package Simulations

import HelperUtils.CloudSimUtils
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import CloudSimWrappers.{ConfiguredBroker, ConfiguredDataCenter, ConfiguredVm}
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.hosts.network.NetworkHost

/**
 * Test Suite to test datacenter creation as per provided configuration
 */
class ConfiguredDatacenterTestSuite extends AnyFlatSpec with Matchers{

  val saasConfig = CloudSimUtils.getconfigValue("Saas","saasDataCenter")

  val testSim = new CloudSim()

  behavior of "Configured Datacenter module"

  "ConfiguredDatacenter" should "Create datacenter with correct costs" in {
    val datacenter = new ConfiguredDataCenter(testSim,saasConfig).getConfiguredDataCenter()

    val dcCharac = datacenter.getCharacteristics
    assert(dcCharac.getCostPerBw == saasConfig.getDouble("costPerBW"))  //check if the bandwidth is same as the config
    assert(dcCharac.getCostPerSecond == saasConfig.getDouble("costPerCPUSecond")) //check if the costPerCPUSecond is same as the config
    assert(dcCharac.getCostPerMem == saasConfig.getDouble("costPerMemory")) //check if the costPerMemory is same as the config
    assert(dcCharac.getCostPerStorage == saasConfig.getDouble("costPerStorage")) //check if the costPerStorage is same as the config
  }

  "ConfiguredDatacenter" should "Have correct policies" in {
    val datacenter = new ConfiguredDataCenter(testSim,saasConfig).getConfiguredDataCenter()

    val dcCharac = datacenter.getCharacteristics
    assert(datacenter.getSchedulingInterval() == saasConfig.getDouble("schedulingInterval"))
    datacenter.getCharacteristics.getOs == saasConfig.getString("OS")
  }

  "ConfiguredDatacenter" should "have all hosts with correct attributes" in {
    val datacenter = new ConfiguredDataCenter(testSim,saasConfig).getConfiguredDataCenter()
    val dcHosts = datacenter.getHostList[NetworkHost]

    assert(dcHosts.size() ==  saasConfig.getInt("host.Count"))

    dcHosts.forEach(host =>{
      assert(host.getMips() == saasConfig.getDouble("host.PemipsCapacity")) //check if the PemipsCapacity is same as the config
      assert(host.getRam().getCapacity() == saasConfig.getLong("host.RAMInMBs")) //check if the RAMInMBs is same as the config
      assert(host.getNumberOfPes() == saasConfig.getDouble("host.Pes")) //check if the Pes is same as the config
    })
  }






}
