module.exports = function MemoryCtrl($scope) {

	var getSdStatus = function() {
		if ($scope.control) {
		  $scope.control.getSdStatus().then(function(result) {
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
            color: d3.scale.category10().range(),
            x: function(d){ return d.x; },
            y: function(d){ return d.y; },
            useInteractiveGuideline: true,
            duration: 0,
            yDomain: [0,100000],
            xAxis: {
                axisLabel: 'polls every 5ms -->'
                /*tickFormat: function(d)*/
            },
            yAxis: {
                axisLabel: 'Consumed',
                tickFormat: function(d){
                   return d3.format('s')(d);
                }
            }
        }
    };
    
    $scope.data = [{ values: [], key: 'Standalone' }, { values: [], key: 'Shared'}];
        
    $scope.run = true;
    
    var x = 0;
    setInterval(function(){

        if (!$scope.run) return;

	    $scope.data[0].values.push({ x: x,	y: $scope.device.cpu.mem_standalone});
        $scope.data[1].values.push({ x: x,  y: $scope.device.cpu.mem_shared});

        if ($scope.data[0].values.length > 20){ 
            $scope.data[0].values.shift();
            $scope.data[1].values.shift();
        }
	    x++;
	    
      $scope.$apply(); // update both chart

    }, 500);
}