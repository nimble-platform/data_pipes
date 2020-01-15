# data_pipes

> ** Internal DataChannel Service **

<a name="getting-started"></a>
## Getting Started

This service enable Nimble's End Users to manage Iot Data in DataMonitor/Data Trading for each contract in Internal DataChannel.

It's used by DataChannel Service to create sensor's topic when a channel has been started by using /manage/createInternalSensorTopic

It's used by Producer in order to write iot Data by using producer/sendIotData

It's used by Consumer in order to read iot Data by using consumer/getNextMessages

This service is going to be integrated with SSO in Nimble (Identity Service) and with new release of DataChannel Service.

Optionally can be enabled database persistence and filtering for production side in properties file for configuration (both disabled on default in this release code).

Whitegoods Swagger can be found at
http://nimblewg.holonix.biz:18889/swagger-ui.html#/rest-consumer

 ---
The project leading to this application has received funding from the European Union Horizon 2020 research and innovation programme under grant agreement No 723810.
