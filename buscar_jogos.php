<?php
/**
 * buscar_jogos.php — QuestGen API v2
 * ────────────────────────────────────────────────────────────────────
 * GET → retorna a lista completa de jogos com mapeamento para camelCase
 * ────────────────────────────────────────────────────────────────────
 */

header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Origin: *");

define("DB_HOST",    "localhost");
define("DB_NAME",    "questgen");
define("DB_USER",    "root");
define("DB_PASS",    "");
define("DB_CHARSET", "utf8mb4");

try {
    $dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
    $pdo = new PDO($dsn, DB_USER, DB_PASS, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::ATTR_EMULATE_PREPARES   => false,
    ]);
} catch (PDOException $e) {
    echo json_encode([
        "status"   => "Erro",
        "mensagem" => "Erro de conexão com o banco de dados: " . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] === "GET") {
    try {
        $stmt = $pdo->query("
            SELECT
                JOGO_ID         AS jogoId,
                JOGO_NOME       AS jogoNome,
                JOGO_IMAGEM_URL AS jogoImagemUrl
            FROM JOGO
            ORDER BY JOGO_NOME ASC
        ");
        $jogos = $stmt->fetchAll();

        foreach ($jogos as &$j) {
            $j["jogoId"] = (int) $j["jogoId"];
            $j["jogoImagemUrl"] = $j["jogoImagemUrl"] ?? "";
        }
        unset($j);

        echo json_encode($jogos, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode([
            "status"   => "Erro",
            "mensagem" => "Erro BD: " . $e->getMessage()
        ], JSON_UNESCAPED_UNICODE);
    }
} else {
    echo json_encode([
        "status"   => "Erro",
        "mensagem" => "Método não suportado. Use GET."
    ], JSON_UNESCAPED_UNICODE);
}
