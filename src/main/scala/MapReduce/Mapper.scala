package MapReduce

import org.cloudbus.cloudsim.cloudlets.CloudletSimple
import org.cloudbus.cloudsim.cloudlets.network.NetworkCloudlet
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModel


class Mapper (length: Long, pesNumber: Int) extends CloudletSimple(length,pesNumber) {
  private val TYPE = "MAPPER"
  def getType = TYPE
}

