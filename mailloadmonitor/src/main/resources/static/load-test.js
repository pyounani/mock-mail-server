const loadTest = {
  tpsChart: undefined
  , processTimeChart: undefined

  , getLoadTestSummary: (row) => {
    const id = row.getAttribute("data-id");  // 클릭한 행의 id 값 가져오기
    loadTest.getTpsData(id);
    loadTest.getProcessTimeData(id);
  }
  , getTpsData: (id) => {
    axios.get(`/load-tests/${id}/tps`)
    .then(response => {
      console.log(response.data);
      loadTest.updateTpsChart(response.data);
    })
    .catch(error => {
      console.error('Error fetching summary:', error);
    });
  }
  , getProcessTimeData: (id) =>{
    axios.get(`/load-tests/${id}/process-time`)
    .then(response => {
      console.log(response.data);
      loadTest.updateProcessTimeChart(response.data);
    })
    .catch(error => {
      console.error('Error fetching summary:', error);
    });
  }
  , updateTpsChart: (tpsData) => {
    const labels = tpsData.map(item => item.time);
    const tps = tpsData.map(item => item.tps);

    // tps 차트
    const tpsCtx = document.getElementById('tpsChart').getContext('2d');
    if (loadTest.tpsChart) {
      loadTest.tpsChart.destroy(); // 이전 차트를 제거합니다.
    }
    loadTest.tpsChart = loadTest.getChart(tpsCtx, labels, tps, 'TPS', 'TPS', 'elapsed time (s)', true);
  }
  , updateProcessTimeChart: (processTimeData) => {
    const labels = processTimeData.map(item => item.index);
    const processTime = processTimeData.map(item => item.second);

    const processTimeCtx = document.getElementById('processingTimeChart').getContext('2d');
    if (loadTest.processTimeChart) {
      loadTest.processTimeChart.destroy(); // 이전 차트를 제거합니다.
    }
    loadTest.processTimeChart = loadTest.getChart(processTimeCtx, labels, processTime, '처리시간', '처리시간', '요청순서', false);
  }
  , getChart: (ctx, labels, data, dataLabel  ,yLabel, xLabel, showLine) => {
  return new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: dataLabel,
        data: data,
        fill: false,
        pointRadius: 3, // 점 크기 설정
        pointHoverRadius: 5, // 마우스 오버 시 점 크기 설정
        borderWidth: 2, // 선 두께 설정
        showLine: showLine,  // 이 옵션을 통해 선을 표시하지 않음
        pointBackgroundColor: data.map(value => value === 0 ? 'red' : 'rgba(75, 192, 192, 0.2)'),
        pointBorderColor: data.map(value => value === 0 ? 'red' : 'rgba(75, 192, 192, 1)')
      }]
    },
    options: {
      responsive: true,
      scales: {
        x: {
          title: {
            display: true,
            text: xLabel,
            font: {
              size: 16 // x축 라벨의 폰트 크기
            }
          }
        },
        y: {
          title: {
            display: true,
            text: yLabel,
            font: {
              size: 16 // x축 라벨의 폰트 크기
            }
          },
          beginAtZero: true
        }
      }
    }
  });
}
}
