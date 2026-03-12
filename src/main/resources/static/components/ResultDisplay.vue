<template>
  <div class="result-display">
    <h2>批改结果</h2>
    
    <!-- 执行状态 -->
    <div class="execution-status">
      <span :class="getStatusClass(submission.status)">
        {{ getStatusText(submission.status) }}
      </span>
    </div>
    
    <!-- 编译结果 -->
    <div v-if="submission.executionResult" class="compile-result">
      <h3>编译结果</h3>
      <div v-if="!submission.executionResult.compileSuccess" class="error">
        <h4>编译错误:</h4>
        <pre>{{ submission.executionResult.compileError }}</pre>
      </div>
      <div v-else class="success">
        <p>✅ 编译成功</p>
      </div>
    </div>
    
    <!-- 运行结果 -->
    <div v-if="submission.executionResult && submission.executionResult.compileSuccess" class="runtime-result">
      <h3>运行结果</h3>
      <div v-if="!submission.executionResult.runtimeSuccess" class="error">
        <h4>运行时错误:</h4>
        <pre>{{ submission.executionResult.runtimeError }}</pre>
      </div>
      <div v-else class="success">
        <p>✅ 程序运行成功</p>
        <p>执行时间: {{ submission.executionResult.executionTime }}ms</p>
        <p>输出结果:</p>
        <pre>{{ submission.executionResult.output }}</pre>
      </div>
    </div>
    
    <!-- AI分析结果 -->
    <div v-if="submission.aiFeedback" class="ai-analysis">
      <h3>AI智能分析</h3>
      <div class="code-quality">
        <p>代码质量评分: {{ submission.aiFeedback.codeQuality }}/100</p>
      </div>
      <div class="suggestions" v-if="submission.aiFeedback.suggestions && submission.aiFeedback.suggestions.length > 0">
        <h4>优化建议:</h4>
        <ul>
          <li v-for="(suggestion, index) in submission.aiFeedback.suggestions" :key="index">
            {{ suggestion }}
          </li>
        </ul>
      </div>
      <div class="explanation" v-if="submission.aiFeedback.explanation">
        <h4>详细解释:</h4>
        <p>{{ submission.aiFeedback.explanation }}</p>
      </div>
    </div>
    
    <!-- 重新提交按钮 -->
    <button @click="retrySubmission" class="retry-button">
      重新批改
    </button>
  </div>
</template>

<script>
export default {
  name: 'ResultDisplay',
  props: {
    submission: {
      type: Object,
      required: true
    }
  },
  methods: {
    getStatusClass(status) {
      switch (status) {
        case 'COMPLETED':
          return 'status-success';
        case 'FAILED':
          return 'status-error';
        case 'PROCESSING':
          return 'status-processing';
        default:
          return 'status-pending';
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'COMPLETED':
          return '批改完成';
        case 'FAILED':
          return '批改失败';
        case 'PROCESSING':
          return '批改中...';
        default:
          return '等待批改';
      }
    },
    retrySubmission() {
      this.$emit('retry', this.submission.id);
    }
  }
}
</script>

<style scoped>
.result-display {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.execution-status {
  text-align: center;
  margin-bottom: 20px;
}

.status-success { color: #28a745; font-weight: bold; }
.status-error { color: #dc3545; font-weight: bold; }
.status-processing { color: #ffc107; font-weight: bold; }
.status-pending { color: #6c757d; font-weight: bold; }

.compile-result, .runtime-result, .ai-analysis {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 5px;
  padding: 15px;
  margin-bottom: 20px;
}

.error {
  color: #dc3545;
}

.success {
  color: #28a745;
}

pre {
  background: #f1f1f1;
  padding: 10px;
  border-radius: 3px;
  overflow-x: auto;
}

.retry-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 16px;
}

.retry-button:hover {
  background: #0056b3;
}
</style>