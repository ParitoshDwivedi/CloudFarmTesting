module.exports = function AnalysisCtrl($scope) {

	var getSdStatus = function() {
		if ($scope.control) {
		  $scope.control.getSdStatus().then(function(result) {
	      	//console.log(result)
	        $scope.$apply(function() {
	          $scope.sdCardMounted = (result.lastData === 'sd_mounted')
	        })
	      })
	    }
	  }
	  getSdStatus()

	$scope.options = {
        chart: {
            type: 'lineChart',
            height: 250,
            margin : {
                top: 20,
                right: 20,
                bottom: 40,
                left: 60
            },
            x: function(d){ return d.x; },
            y: function(d){ return d.y; },
            useInteractiveGuideline: true,
            duration: 0,
            yDomain: [0,100],
            xAxis: {
                axisLabel: 'polls every 5ms -->'
                /*tickFormat: function(d)*/
            },
            yAxis: {
                axisLabel: 'Utilization',
                tickFormat: function(d){
                   return d3.format('%')(d/100);
                }
            }
        }
    };
    
    $scope.data = [{ values: [], key: 'CPU Usage' }];
        
    $scope.run = true;
    
    var x = 0;
    setInterval(function(){
	    if (!$scope.run) return;
	    $scope.data[0].values.push({ x: x,	y: $scope.device.cpu.cpu_consumed});
      if ($scope.data[0].values.length > 20) $scope.data[0].values.shift();
	    x++;
	    
      $scope.$apply(); // update both chart
    }, 500);
}