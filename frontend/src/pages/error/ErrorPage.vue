<template>
  <div class="centered-container">
    <img src="../../assets/img/error-message.png" alt="Centered Image" class="centered-image">
    <h1>관리자에게 문의해주세요. <a href="/">☞ 메인으로</a></h1>
  </div>
</template>

<script setup>
</script>

<style scoped>
.centered-container {
  display: flex;
  flex-direction: column;
  justify-content: center; 
  align-items: center;
  height: 100vh;
}

.centered-image {
  max-width: 80%;
  max-height: 80%;
  object-fit: contain;
}

h1 {
  margin-top: 20px;
  font-size: 1.5em;
  color: #333;
}
</style>
